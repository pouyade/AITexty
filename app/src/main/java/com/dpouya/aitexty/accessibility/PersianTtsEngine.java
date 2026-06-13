package com.dpouya.aitexty.accessibility;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;

import com.k2fsa.sherpa.onnx.OfflineTts;
import com.k2fsa.sherpa.onnx.OfflineTtsConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.jvm.functions.Function1;

public final class PersianTtsEngine {
    private static final String TAG = "PersianTtsEngine";

    public static final int SUCCESS = TextToSpeech.SUCCESS;
    public static final int ERROR = TextToSpeech.ERROR;
    public static final int QUEUE_ADD = TextToSpeech.QUEUE_ADD;
    public static final int QUEUE_FLUSH = TextToSpeech.QUEUE_FLUSH;

    private static PersianTtsEngine instance;

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "PersianTtsEngine");
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });
    private final AtomicBoolean taskCancelled = new AtomicBoolean(false);

    private final AtomicBoolean nativeLoadInProgress = new AtomicBoolean(false);

    private OfflineTts offlineTts;
    private AudioTrack audioTrack;
    private PersianTtsVoice activeVoice = PersianTtsVoice.GYRO;
    private boolean ready;
    private boolean initFailed;
    private boolean initStarted;
    private String pendingText;
    private int pendingQueueMode = QUEUE_FLUSH;
    private String lastError;

    private PersianTtsEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized PersianTtsEngine getInstance(Context context) {
        if (instance == null) {
            instance = new PersianTtsEngine(context);
        }
        return instance;
    }

    public synchronized void ensureInitialized() {
        ensureInitialized(PersianTtsVoice.fromSettingValue(AccessibilitySettings.getVoiceName()));
    }

    public synchronized void ensureInitialized(PersianTtsVoice voice) {
        if (initStarted && voice == activeVoice && ready) {
            return;
        }
        if (initStarted && voice == activeVoice && initFailed) {
            initFailed = false;
            prepareAndInit(voice);
            return;
        }
        if (initStarted && voice != activeVoice) {
            setVoice(voice);
            return;
        }
        initStarted = true;
        activeVoice = voice;
        prepareAndInit(voice);
    }

    public synchronized void setVoice(PersianTtsVoice voice) {
        if (voice == null) {
            voice = PersianTtsVoice.GYRO;
        }
        if (ready && voice == activeVoice) {
            return;
        }
        shutdownEngine();
        initStarted = true;
        initFailed = false;
        ready = false;
        activeVoice = voice;
        prepareAndInit(voice);
    }

    private void prepareAndInit(PersianTtsVoice voice) {
        Log.i(TAG, "Preparing Persian voice: " + voice.id);
        PersianTtsModelStore.prepareVoice(context, voice, new PersianTtsModelStore.PrepareProgressCallback() {
            @Override
            public void onPhase(String messageKey) {
                // Dialog updates handled in PersianTtsModelStore
            }

            @Override
            public void onProgress(int percent) {
                // Dialog updates handled in PersianTtsModelStore
            }

            @Override
            public void onReady() {
                executor.execute(() -> initOnWorker(voice));
            }

            @Override
            public void onError(String message) {
                lastError = message;
                initFailed = true;
                ready = false;
                Log.e(TAG, "Voice prepare failed: " + message);
                mainHandler.post(() -> {
                    Activity activity = getProgressActivity();
                    if (activity != null) {
                        PersianTtsDownloadDialog.showError(activity, message);
                    }
                });
            }
        });
    }

    private Activity getProgressActivity() {
        return PersianTtsModelStore.getProgressActivity();
    }

    public void setProgressActivity(Activity activity) {
        PersianTtsModelStore.setProgressActivity(activity);
    }

    private void initOnWorker(PersianTtsVoice voice) {
        if (!nativeLoadInProgress.compareAndSet(false, true)) {
            return;
        }
        try {
            shutdownEngine();
            if (!PersianTtsModelStore.validateVoiceModelStrict(context, voice)) {
                PersianTtsModelStore.deleteVoiceModel(context, voice);
                throw new IllegalStateException("Invalid or corrupt voice model: " + voice.id);
            }
            if (!PersianTtsProbeClient.probeBlocking(context, voice)) {
                PersianTtsModelStore.deleteVoiceModel(context, voice);
                throw new IllegalStateException("Voice model failed native load probe: " + voice.id);
            }
            OfflineTtsConfig config = PersianTtsConfigBuilder.build(context, voice);
            offlineTts = new OfflineTts(null, config);
            if (offlineTts.numSpeakers() <= 0) {
                throw new IllegalStateException("No speakers in model " + voice.id);
            }
            initAudioTrack(AudioManager.STREAM_MUSIC);
            ready = true;
            initFailed = false;
            Log.i(TAG, "Persian voice ready: " + voice.id);
            mainHandler.post(this::playPendingIfAny);
        } catch (Throwable t) {
            handleInitFailure(voice, t);
        } finally {
            nativeLoadInProgress.set(false);
        }
    }

    private void handleInitFailure(PersianTtsVoice voice, Throwable t) {
        ready = false;
        initFailed = true;
        lastError = t.getMessage();
        PersianTtsModelStore.deleteVoiceModel(context, voice);
        Log.e(TAG, "initOnWorker failed for " + voice.id, t);
        if (voice != PersianTtsVoice.GYRO) {
            AccessibilitySettings.setVoiceName(PersianTtsVoice.GYRO.getSettingValue());
            activeVoice = PersianTtsVoice.GYRO;
        }
        mainHandler.post(() -> {
            Activity activity = getProgressActivity();
            if (activity != null) {
                PersianTtsDownloadDialog.showError(activity,
                        lastError != null ? lastError : "Voice model failed to load");
            }
        });
    }

    private void initAudioTrack(int streamType) {
        if (offlineTts == null) {
            return;
        }
        shutdownAudioTrack();
        int sampleRate = offlineTts.sampleRate();
        audioTrack = new AudioTrack(
                buildAudioAttributes(streamType),
                new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setSampleRate(sampleRate)
                        .build(),
                AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT),
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );
    }

    private AudioAttributes buildAudioAttributes(int streamType) {
        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        switch (streamType) {
            case AudioManager.STREAM_MUSIC:
                builder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
                builder.setUsage(AudioAttributes.USAGE_MEDIA);
                break;
            case AudioManager.STREAM_VOICE_CALL:
                builder.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
                builder.setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION);
                break;
            default:
                builder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
                builder.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION);
                break;
        }
        return builder.build();
    }

    public PersianTtsVoice getActiveVoice() {
        return activeVoice;
    }

    public boolean isReady() {
        return ready;
    }

    public boolean hasInitFailed() {
        return initFailed;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isVoiceReady(PersianTtsVoice voice) {
        return PersianTtsModelStore.isVoiceReady(context, voice);
    }

    public void whenReady(SpeechHelper.InitCallback callback) {
        if (ready) {
            callback.onReady(true);
            return;
        }
        if (initFailed) {
            callback.onReady(false);
            return;
        }
        PersianTtsVoice active = PersianTtsVoice.fromSettingValue(AccessibilitySettings.getVoiceName());
        boolean filesReady = PersianTtsModelStore.isVoiceReady(context, active);
        callback.onReady(filesReady || ready);
    }

    public void applySpeechRate() {
        // speech rate applied per utterance
    }

    public synchronized void speak(String text, int queueMode) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        ensureInitialized(PersianTtsVoice.fromSettingValue(AccessibilitySettings.getVoiceName()));
        if (!ready || offlineTts == null || audioTrack == null) {
            pendingText = text;
            pendingQueueMode = queueMode;
            return;
        }
        if (queueMode == QUEUE_FLUSH) {
            stopInternal();
        }
        final String speakText = text;
        taskCancelled.set(false);
        executor.execute(() -> synthesize(speakText));
    }

    private void synthesize(String text) {
        try {
            if (offlineTts == null || audioTrack == null || taskCancelled.get()) {
                return;
            }
            if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.play();
            }
            float speed = AccessibilitySettings.getSpeechRate();
            Function1<float[], Integer> callback = samples -> {
                if (taskCancelled.get()) {
                    return 0;
                }
                audioTrack.write(samples.clone(), 0, samples.length, AudioTrack.WRITE_BLOCKING);
                return 1;
            };
            offlineTts.generateWithCallback(text, 0, speed, callback);
        } catch (Throwable t) {
            Log.e(TAG, "synthesize failed", t);
        }
    }

    private void playPendingIfAny() {
        if (!TextUtils.isEmpty(pendingText) && ready) {
            String text = pendingText;
            int queueMode = pendingQueueMode;
            pendingText = null;
            speak(text, queueMode);
        }
    }

    public synchronized void stop() {
        pendingText = null;
        stopInternal();
    }

    private void stopInternal() {
        taskCancelled.set(true);
        if (audioTrack != null) {
            try {
                audioTrack.pause();
                audioTrack.flush();
                audioTrack.stop();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void shutdown() {
        stop();
        shutdownEngine();
        ready = false;
        initFailed = false;
        initStarted = false;
        pendingText = null;
    }

    private void shutdownEngine() {
        shutdownAudioTrack();
        if (offlineTts != null) {
            try {
                offlineTts.release();
            } catch (Exception ignored) {
            }
            offlineTts = null;
        }
    }

    private void shutdownAudioTrack() {
        if (audioTrack == null) {
            return;
        }
        try {
            audioTrack.pause();
            audioTrack.flush();
            audioTrack.stop();
        } catch (Exception ignored) {
        }
        try {
            audioTrack.release();
        } catch (Exception ignored) {
        }
        audioTrack = null;
    }
}

package com.dpouya.aitexty.accessibility;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.k2fsa.sherpa.onnx.OfflineTts;

/**
 * Loads ONNX models in an isolated process so native load failures cannot kill the UI process.
 */
public final class PersianTtsProbeService extends Service {
    private static final String TAG = "PersianTtsProbe";

    static final int MSG_PROBE = 1;
    static final int MSG_RESULT = 2;

    private HandlerThread workerThread;
    private Messenger incomingMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        workerThread = new HandlerThread("PersianTtsProbeWorker");
        workerThread.start();
        incomingMessenger = new Messenger(new IncomingHandler(workerThread.getLooper()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return incomingMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        if (workerThread != null) {
            workerThread.quitSafely();
            workerThread = null;
        }
        super.onDestroy();
    }

    private final class IncomingHandler extends Handler {
        IncomingHandler(android.os.Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_PROBE) {
                return;
            }
            String voiceId = msg.getData().getString(PersianTtsProbeClient.EXTRA_VOICE_ID);
            PersianTtsVoice voice = PersianTtsVoice.fromId(voiceId);
            boolean success = false;
            String error = null;
            OfflineTts tts = null;
            try {
                if (!PersianTtsModelStore.validateVoiceModelStrict(PersianTtsProbeService.this, voice)) {
                    throw new IllegalStateException("Model validation failed for " + voice.id);
                }
                tts = new OfflineTts(null, PersianTtsConfigBuilder.build(PersianTtsProbeService.this, voice));
                if (tts.numSpeakers() <= 0) {
                    throw new IllegalStateException("No speakers in model " + voice.id);
                }
                success = true;
            } catch (Throwable t) {
                error = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                Log.e(TAG, "Probe failed for " + voice.id, t);
            } finally {
                if (tts != null) {
                    try {
                        tts.release();
                    } catch (Exception ignored) {
                    }
                }
            }
            if (msg.replyTo != null) {
                Message reply = Message.obtain(null, MSG_RESULT, success ? 1 : 0, 0);
                if (error != null) {
                    reply.getData().putString(PersianTtsProbeClient.EXTRA_ERROR, error);
                }
                try {
                    msg.replyTo.send(reply);
                } catch (RemoteException e) {
                    Log.w(TAG, "Could not send probe result", e);
                }
            }
        }
    }
}

package com.dpouya.aitexty.accessibility;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.TextUtils;
import android.util.Log;

import com.dpouya.aitexty.helper.LocaleController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SpeechHelper implements TextToSpeech.OnInitListener {
    private static final String TAG = "SpeechHelper";
    private static final String GOOGLE_TTS_ENGINE = "com.google.android.tts";
    private static final String SAMSUNG_TTS_ENGINE = "com.samsung.SMT";
    private static final String BUILTIN_PERSIAN_VOICE = "builtin_persian";
    private static final String[] PERSIAN_TTS_ENGINES = {
            GOOGLE_TTS_ENGINE,
            SAMSUNG_TTS_ENGINE
    };

    public static class VoiceOption {
        public final String name;
        public final String label;

        public VoiceOption(String name, String label) {
            this.name = name;
            this.label = label;
        }
    }

    public interface InitCallback {
        void onReady(boolean success);
    }

    private static SpeechHelper instance;

    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private boolean ready;
    private String pendingSpeakText;
    private int pendingQueueMode = TextToSpeech.QUEUE_FLUSH;
    private Locale lastPreparedLocale;
    private String activeEngine;

    private SpeechHelper(Context context) {
        this.context = context.getApplicationContext();
        createTts(selectInitialEngine());
    }

    public static synchronized SpeechHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SpeechHelper(context);
        }
        return instance;
    }

    private String selectInitialEngine() {
        for (String engine : PERSIAN_TTS_ENGINES) {
            if (isEngineInstalled(engine)) {
                return engine;
            }
        }
        return null;
    }

    private void createTts(String enginePackage) {
        ready = false;
        if (tts != null) {
            try {
                tts.stop();
                tts.shutdown();
            } catch (Exception ignored) {
            }
            tts = null;
        }
        activeEngine = enginePackage;
        if (!TextUtils.isEmpty(enginePackage)) {
            Log.i(TAG, "Initializing system TTS engine: " + enginePackage);
            tts = new TextToSpeech(context, this, enginePackage);
        } else {
            Log.i(TAG, "Initializing default system TTS engine");
            tts = new TextToSpeech(context, this);
        }
    }

    private boolean isEngineInstalled(String enginePackage) {
        if (TextUtils.isEmpty(enginePackage)) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(enginePackage, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onInit(int status) {
        ready = status == TextToSpeech.SUCCESS;
        if (ready && tts != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.getVoices();
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                    }

                    @Override
                    @Deprecated
                    public void onError(String utteranceId) {
                        Log.w(TAG, "TTS utterance error: " + utteranceId);
                    }

                    @Override
                    public void onError(String utteranceId, int errorCode) {
                        Log.w(TAG, "TTS utterance error: " + utteranceId + " code=" + errorCode);
                    }
                });
            }
            applySettings();
            if (!TextUtils.isEmpty(pendingSpeakText)) {
                String text = pendingSpeakText;
                int queueMode = pendingQueueMode;
                pendingSpeakText = null;
                speakInternal(text, queueMode, false);
            }
        } else if (!TextUtils.isEmpty(pendingSpeakText)) {
            String text = pendingSpeakText;
            int queueMode = pendingQueueMode;
            pendingSpeakText = null;
            if (SpeechLocaleHelper.isPersianLocale(TextLanguageDetector.detect(text))
                    && tryNextPersianEngine()) {
                pendingSpeakText = text;
                pendingQueueMode = queueMode;
            }
        }
    }

    public boolean isReady() {
        return ready || PersianTtsEngine.getInstance(context).isReady();
    }

    public void whenReady(InitCallback callback) {
        PersianTtsEngine persian = PersianTtsEngine.getInstance(context);
        if (persian.isReady() || ready) {
            callback.onReady(true);
            return;
        }
        persian.whenReady(callback);
    }

    public boolean isPersianLanguageAvailable() {
        if (PersianTtsEngine.getInstance(context).isReady()) {
            return true;
        }
        if (!ready || tts == null) {
            return false;
        }
        return isLocaleAvailable(Locale.forLanguageTag("fa-IR"))
                || isLocaleAvailable(new Locale("fa"));
    }

    public boolean isAppLanguageAvailable() {
        if (SpeechLocaleHelper.isPersianAppLanguage()) {
            return isPersianLanguageAvailable();
        }
        if (!ready || tts == null) {
            return PersianTtsEngine.getInstance(context).isReady();
        }
        return isLocaleAvailable(Locale.US)
                || isLocaleAvailable(Locale.getDefault());
    }

    private boolean isLocaleAvailable(Locale locale) {
        if (tts == null || locale == null) {
            return false;
        }
        int result = tts.isLanguageAvailable(locale);
        return result >= TextToSpeech.LANG_AVAILABLE;
    }

    public void openInstallTtsData() {
        if (PersianTtsEngine.getInstance(context).isReady()) {
            return;
        }
        if (!isEngineInstalled(GOOGLE_TTS_ENGINE)) {
            openGoogleTtsInStore();
            return;
        }
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.setPackage(GOOGLE_TTS_ENGINE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setPackage(null);
            context.startActivity(intent);
        }
    }

    public void openGoogleTtsInStore() {
        try {
            Intent market = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + GOOGLE_TTS_ENGINE));
            market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(market);
        } catch (ActivityNotFoundException e) {
            Intent web = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + GOOGLE_TTS_ENGINE));
            web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(web);
        }
    }

    public void applySettings() {
        PersianTtsEngine.getInstance(context).applySpeechRate();
        if (!ready || tts == null) {
            return;
        }
        tts.setSpeechRate(AccessibilitySettings.getSpeechRate());
    }

    public void speak(String text) {
        speakInternal(text, TextToSpeech.QUEUE_FLUSH, true);
    }

    public void speakQueued(String text) {
        speakInternal(text, TextToSpeech.QUEUE_ADD, true);
    }

    private void speakInternal(String text, int queueMode, boolean allowEngineSwitch) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Locale detected = TextLanguageDetector.detect(text);
        if (shouldUseBuiltinPersian(detected)) {
            PersianTtsEngine persian = PersianTtsEngine.getInstance(context);
            if (queueMode == TextToSpeech.QUEUE_FLUSH) {
                stopSystemTts();
                pendingSpeakText = null;
            }
            persian.speak(text, queueMode);
            return;
        }

        if (!ready || tts == null) {
            pendingSpeakText = text;
            pendingQueueMode = queueMode;
            return;
        }

        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            PersianTtsEngine.getInstance(context).stop();
        }

        tts.setSpeechRate(AccessibilitySettings.getSpeechRate());
        boolean prepared = prepareForSpeech(detected);

        if (!prepared) {
            if (SpeechLocaleHelper.isPersianLocale(detected)) {
                if (allowEngineSwitch && tryNextPersianEngine()) {
                    pendingSpeakText = text;
                    pendingQueueMode = queueMode;
                    return;
                }
                Log.w(TAG, "Persian TTS unavailable on system engine "
                        + (activeEngine != null ? activeEngine : "default"));
            } else if (!prepareForSpeech(Locale.US)) {
                prepareForSpeech(Locale.getDefault());
            }
        }

        doSpeak(text, queueMode);
    }

    private boolean shouldUseBuiltinPersian(Locale locale) {
        if (!SpeechLocaleHelper.isPersianLocale(locale)) {
            return false;
        }
        String preferredVoice = AccessibilitySettings.getVoiceName();
        if (TextUtils.isEmpty(preferredVoice) || preferredVoice.startsWith("persian:")
                || BUILTIN_PERSIAN_VOICE.equals(preferredVoice)) {
            return true;
        }
        return false;
    }

    private void stopSystemTts() {
        if (tts != null) {
            tts.stop();
        }
    }

    private boolean tryNextPersianEngine() {
        for (String engine : PERSIAN_TTS_ENGINES) {
            if (engine.equals(activeEngine) || !isEngineInstalled(engine)) {
                continue;
            }
            Log.i(TAG, "Switching system TTS engine to " + engine + " for Persian");
            createTts(engine);
            return true;
        }
        return false;
    }

    private boolean prepareForSpeech(Locale locale) {
        if (tts == null || locale == null) {
            return false;
        }

        Locale[] candidates = buildLocaleCandidates(locale);
        for (Locale candidate : candidates) {
            int result = tts.setLanguage(candidate);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                lastPreparedLocale = candidate;
                applyVoiceForLocale(candidate);
                ensureCompatibleVoice(candidate);
                return true;
            }
        }
        lastPreparedLocale = null;
        return false;
    }

    private Locale[] buildLocaleCandidates(Locale locale) {
        if (SpeechLocaleHelper.isPersianLocale(locale)) {
            return new Locale[]{
                    Locale.forLanguageTag("fa-IR"),
                    new Locale("fa", "IR"),
                    new Locale("fa"),
                    locale
            };
        }
        if ("en".equalsIgnoreCase(locale.getLanguage())) {
            return new Locale[]{Locale.US, Locale.UK, locale};
        }
        return new Locale[]{locale, new Locale(locale.getLanguage())};
    }

    private void applyVoiceForLocale(Locale locale) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        String preferredVoice = AccessibilitySettings.getVoiceName();
        if (!TextUtils.isEmpty(preferredVoice)) {
            Voice preferred = findVoiceByName(preferredVoice);
            if (preferred != null && SpeechLocaleHelper.voiceMatchesLocale(preferred, locale)) {
                tts.setVoice(preferred);
                return;
            }
        }
        selectBestVoiceForLocale(locale);
    }

    private void ensureCompatibleVoice(Locale locale) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || tts == null) {
            return;
        }

        Voice current = tts.getVoice();
        if (current != null && SpeechLocaleHelper.voiceMatchesLocale(current, locale)) {
            return;
        }

        Set<Voice> voices = getVoices();
        if (voices == null || voices.isEmpty()) {
            return;
        }

        if (selectBestVoiceForLocale(locale) || selectVoiceByNamePattern(locale)) {
            return;
        }

        if (current != null && !SpeechLocaleHelper.voiceMatchesLocale(current, locale)
                && lastPreparedLocale != null) {
            tts.setLanguage(lastPreparedLocale);
        }
    }

    private Voice findVoiceByName(String voiceName) {
        Set<Voice> voices = getVoices();
        if (voices == null || TextUtils.isEmpty(voiceName)) {
            return null;
        }
        for (Voice voice : voices) {
            if (voiceName.equals(voice.getName())) {
                return voice;
            }
        }
        return null;
    }

    private Set<Voice> getVoices() {
        if (tts == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        return tts.getVoices();
    }

    private boolean selectBestVoiceForLocale(Locale targetLocale) {
        Set<Voice> voices = getVoices();
        if (voices == null || voices.isEmpty()) {
            return false;
        }

        Voice offlineMatch = null;
        Voice anyMatch = null;
        for (Voice voice : voices) {
            if (!SpeechLocaleHelper.voiceMatchesLocale(voice, targetLocale)) {
                continue;
            }
            anyMatch = voice;
            if (!voice.isNetworkConnectionRequired()) {
                offlineMatch = voice;
                break;
            }
        }

        Voice selected = offlineMatch != null ? offlineMatch : anyMatch;
        if (selected != null) {
            tts.setVoice(selected);
            return true;
        }
        return false;
    }

    private boolean selectVoiceByNamePattern(Locale targetLocale) {
        Set<Voice> voices = getVoices();
        if (voices == null || targetLocale == null) {
            return false;
        }

        Voice offlineMatch = null;
        Voice anyMatch = null;
        for (Voice voice : voices) {
            if (!SpeechLocaleHelper.voiceNameMatchesLocale(voice.getName(), targetLocale)) {
                continue;
            }
            anyMatch = voice;
            if (!voice.isNetworkConnectionRequired()) {
                offlineMatch = voice;
                break;
            }
        }

        Voice selected = offlineMatch != null ? offlineMatch : anyMatch;
        if (selected != null) {
            tts.setVoice(selected);
            return true;
        }
        return false;
    }

    private void doSpeak(String text, int queueMode) {
        String utteranceId = "aitexty_" + System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            int result = tts.speak(text, queueMode, params, utteranceId);
            if (result == TextToSpeech.ERROR) {
                Log.w(TAG, "TTS speak returned ERROR for locale "
                        + TextLanguageDetector.detect(text).toLanguageTag()
                        + " engine=" + (activeEngine != null ? activeEngine : "default")
                        + " voice=" + (tts.getVoice() != null ? tts.getVoice().getName() : "null"));
            }
        } else {
            tts.speak(text, queueMode, null);
        }
    }

    public void stop() {
        pendingSpeakText = null;
        PersianTtsEngine.getInstance(context).stop();
        if (tts != null) {
            tts.stop();
        }
    }

    public List<VoiceOption> getAvailableVoices() {
        List<VoiceOption> options = new ArrayList<>();
        options.add(new VoiceOption("", LocaleController.getString("tts_auto_detect")));

        PersianTtsEngine persianEngine = PersianTtsEngine.getInstance(context);
        for (PersianTtsVoice voice : PersianTtsVoice.values()) {
            String label = voice.getLabel();
            if (!persianEngine.isVoiceReady(voice) && voice.downloadArchive != null) {
                label += " (" + LocaleController.getString("persian_voice_download") + ")";
            }
            options.add(new VoiceOption(voice.getSettingValue(), label));
        }

        if (!ready || tts == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return options;
        }
        Set<Voice> voices = getVoices();
        if (voices == null || voices.isEmpty()) {
            return options;
        }

        List<VoiceOption> persianVoices = new ArrayList<>();
        List<VoiceOption> englishVoices = new ArrayList<>();
        List<VoiceOption> otherVoices = new ArrayList<>();
        Locale displayLocale = SpeechLocaleHelper.getDisplayLocale();

        for (Voice voice : voices) {
            VoiceOption option = new VoiceOption(voice.getName(), formatVoiceLabel(voice, displayLocale));
            if (SpeechLocaleHelper.isPersianVoice(voice)) {
                persianVoices.add(option);
            } else if (voice.getLocale() != null
                    && "en".equalsIgnoreCase(voice.getLocale().getLanguage())) {
                englishVoices.add(option);
            } else {
                otherVoices.add(option);
            }
        }

        Comparator<VoiceOption> byLabel = Comparator.comparing(o -> o.label);
        Collections.sort(persianVoices, byLabel);
        Collections.sort(englishVoices, byLabel);
        Collections.sort(otherVoices, byLabel);

        options.addAll(persianVoices);
        options.addAll(englishVoices);
        options.addAll(otherVoices);
        return options;
    }

    private String formatVoiceLabel(Voice voice, Locale displayLocale) {
        Locale locale = voice.getLocale();
        StringBuilder label = new StringBuilder();
        if (locale != null) {
            label.append(locale.getDisplayName(displayLocale));
        } else {
            label.append(LocaleController.getString("voice_default"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && voice.isNetworkConnectionRequired()) {
            label.append(" (").append(LocaleController.getString("online_voice")).append(")");
        }
        return label.toString();
    }

    public void shutdown() {
        PersianTtsEngine.getInstance(context).shutdown();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        ready = false;
        pendingSpeakText = null;
        lastPreparedLocale = null;
        activeEngine = null;
    }
}

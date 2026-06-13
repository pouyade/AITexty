package com.dpouya.aitexty.accessibility;

import android.app.Activity;

public class VoiceCommandController {
    public interface Host {
        void onVoiceSendMessage();

        void onVoiceReadMessage();

        void onVoiceReadAllMessages();

        void onVoiceFindChat(String query);

        void onVoiceGoBack();

        void onVoiceDictation(String text);
    }

    private final Activity activity;
    private final SpeechRecognitionHelper recognitionHelper;
    private Host host;
    private boolean commandMode;

    public VoiceCommandController(Activity activity) {
        this.activity = activity;
        this.recognitionHelper = new SpeechRecognitionHelper(activity);
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public boolean isListening() {
        return recognitionHelper.isListening();
    }

    public void startDictation() {
        commandMode = false;
        listen();
    }

    public void startCommandListening() {
        commandMode = true;
        listen();
    }

    public void stop() {
        recognitionHelper.stopListening();
    }

    private void listen() {
        recognitionHelper.startListening(activity, new SpeechRecognitionHelper.Callback() {
            @Override
            public void onResult(String text) {
                if (host == null) {
                    return;
                }
                if (commandMode) {
                    dispatchCommand(VoiceCommandParser.parse(text));
                } else {
                    host.onVoiceDictation(text);
                }
            }

            @Override
            public void onError(String error) {
                SpeechHelper.getInstance(activity).speak(error);
            }
        });
    }

    private void dispatchCommand(VoiceCommandParser.ParsedCommand command) {
        switch (command.type) {
            case SEND_MESSAGE:
                host.onVoiceSendMessage();
                break;
            case READ_MESSAGE:
                host.onVoiceReadMessage();
                break;
            case READ_ALL_MESSAGES:
                host.onVoiceReadAllMessages();
                break;
            case FIND_CHAT:
                host.onVoiceFindChat(command.argument);
                break;
            case GO_BACK:
                host.onVoiceGoBack();
                break;
            case DICTATION:
            default:
                host.onVoiceDictation(command.argument);
                break;
        }
    }
}

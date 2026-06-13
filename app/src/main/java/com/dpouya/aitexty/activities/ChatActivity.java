package com.dpouya.aitexty.activities;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.accessibility.AccessibilitySettings;
import com.dpouya.aitexty.accessibility.PersianTtsEngine;
import com.dpouya.aitexty.accessibility.SpeechHelper;
import com.dpouya.aitexty.accessibility.VoiceCommandController;
import com.dpouya.aitexty.ai.AutoResponseAgent;
import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.components.FaIconView;
import com.dpouya.aitexty.data.SmsRepository;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.ContactHelper;
import com.dpouya.aitexty.helper.FontAwesome;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.PermissionHelper;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.ActionBar.ActionBarButton;
import com.dpouya.aitexty.ui.ChatThemeManager;
import com.dpouya.aitexty.ui.adapter.MessageAdapter;

import java.util.List;
import java.util.Map;

public class ChatActivity extends BaseActivity implements VoiceCommandController.Host {
    private long conversationId;
    private SmsRepository repository;
    private MessageAdapter adapter;
    private EditText composeField;
    private AutoResponseAgent autoAgent;
    private RecyclerView messageList;
    private FaIconView micBtn;
    private VoiceCommandController voiceController;
    private boolean pendingVoiceAction;

    @Override
    protected View initViews(Context context) {
        conversationId = getIntent().getLongExtra("conversation_id", -1);
        String address = getIntent().getStringExtra("address");
        repository = SmsRepository.getInstance(context);
        autoAgent = new AutoResponseAgent(context);
        voiceController = new VoiceCommandController(this);
        voiceController.setHost(this);

        if (conversationId < 0 && !TextUtils.isEmpty(address)) {
            Conversation conv = repository.getOrCreateConversation(address, address);
            conversationId = conv.id;
        }

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(ChatThemeManager.getBackgroundColor(context, conversationId));

        RecyclerView list = new RecyclerView(context);
        messageList = list;
        list.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MessageAdapter(context);
        adapter.setShowSpeakButton(AccessibilitySettings.isSpeakButtonEnabled());
        adapter.setSpeakListener(message -> {
            if (message.body != null) {
                SpeechHelper.getInstance(this).speak(message.body);
            }
        });
        Map<String, Integer> colors = ChatThemeManager.getEffectiveColors(context, conversationId);
        adapter.setBubbleColors(
                colors.get(Theme.MESSAGE_INCOMING_BG),
                colors.get(Theme.MESSAGE_OUTGOING_BG));
        list.setAdapter(adapter);
        root.addView(list, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP, 0, 0, 0, 60));

        LinearLayout composeBar = new LinearLayout(context);
        composeBar.setOrientation(LinearLayout.HORIZONTAL);
        composeBar.setBackgroundColor(Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
        composeBar.setPadding(AndroidUtilities.dp5, AndroidUtilities.dp5, AndroidUtilities.dp5, AndroidUtilities.dp5);

        composeField = new EditText(context);
        composeField.setHint(LocaleController.getString("compose_hint"));
        composeBar.addView(composeField, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        micBtn = new FaIconView(context, 20);
        micBtn.setIcon(FontAwesome.Icon.MICROPHONE);
        micBtn.setIconColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        micBtn.setVisibility(AccessibilitySettings.isSttComposeEnabled() ? View.VISIBLE : View.GONE);
        micBtn.setOnClickListener(v -> startDictation());
        composeBar.addView(micBtn, LayoutHelper.createLinear(40, 40, 0, 4, 0, 0));

        FaIconView suggestBtn = new FaIconView(context, 20);
        suggestBtn.setIcon(FontAwesome.Icon.ROBOT);
        suggestBtn.setIconColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        suggestBtn.setOnClickListener(v -> autoAgent.suggestReply(conversationId, new com.dpouya.aitexty.ai.LlamaEngine.StreamCallback() {
            @Override
            public void onToken(String token) {
            }

            @Override
            public void onComplete(String full) {
                composeField.setText(full.trim());
            }

            @Override
            public void onError(String error) {
            }
        }));
        composeBar.addView(suggestBtn, LayoutHelper.createLinear(40, 40, 0, 4, 0, 0));

        FaIconView sendBtn = new FaIconView(context, 20);
        sendBtn.setIcon(FontAwesome.Icon.PAPER_PLANE);
        sendBtn.setIconColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        sendBtn.setOnClickListener(v -> sendCurrentMessage());
        composeBar.addView(sendBtn, LayoutHelper.createLinear(40, 40, 0, 4, 0, 0));

        root.addView(composeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceiveSms);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didAccessibilitySettingsChanged);
        refreshMessages();
        return root;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        if (repository == null || conversationId < 0) {
            bar.showBackButton(true);
            bar.setOnIconClick(v -> finish());
            return;
        }
        Conversation conv = repository.getConversation(conversationId);
        String displayName = "";
        if (conv != null) {
            displayName = conv.displayName != null ? conv.displayName : conv.address;
            if (displayName == null || displayName.equals(conv.address)) {
                displayName = ContactHelper.resolveDisplayName(this, conv.address);
            }
        }
        String title = displayName;
        if (conv != null && conv.isEncrypted) {
            title = "🔒 " + title;
        }
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(title);
        if (conv != null) {
            bar.setContactAvatar(conv.address, displayName);
        }
        bar.addButton(new ActionBarButton(FontAwesome.Icon.LOCK,
                Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR), v -> {
            Intent i = new Intent(this, EncryptChatActivity.class);
            i.putExtra("conversation_id", conversationId);
            startActivity(i);
        }));
        if (AccessibilitySettings.isVoiceControlEnabled()) {
            bar.addButton(new ActionBarButton(FontAwesome.Icon.MICROPHONE,
                    Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR), v -> startVoiceCommands()));
        }
    }

    private void sendCurrentMessage() {
        String text = composeField.getText().toString().trim();
        if (!text.isEmpty()) {
            repository.sendMessage(conversationId, text);
            composeField.setText("");
        }
    }

    private void startDictation() {
        if (!PermissionHelper.requestRecordAudio(this)) {
            pendingVoiceAction = false;
            return;
        }
        if (voiceController.isListening()) {
            voiceController.stop();
            return;
        }
        Toast.makeText(this, LocaleController.getString("voice_listening"), Toast.LENGTH_SHORT).show();
        voiceController.startDictation();
    }

    private void startVoiceCommands() {
        if (!PermissionHelper.requestRecordAudio(this)) {
            pendingVoiceAction = true;
            return;
        }
        if (voiceController.isListening()) {
            voiceController.stop();
            return;
        }
        Toast.makeText(this, LocaleController.getString("voice_listening"), Toast.LENGTH_SHORT).show();
        voiceController.startCommandListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQUEST_RECORD_AUDIO && PermissionHelper.hasRecordAudio(this)) {
            if (pendingVoiceAction) {
                pendingVoiceAction = false;
                startVoiceCommands();
            }
        }
    }

    @Override
    public void onVoiceSendMessage() {
        sendCurrentMessage();
    }

    @Override
    public void onVoiceReadMessage() {
        List<Message> messages = adapter.getItems();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.type == Telephony.Sms.MESSAGE_TYPE_INBOX && message.body != null) {
                SpeechHelper.getInstance(this).speak(message.body);
                return;
            }
        }
        SpeechHelper.getInstance(this).speak(LocaleController.getString("no_conversations"));
    }

    @Override
    public void onVoiceReadAllMessages() {
        SpeechHelper helper = SpeechHelper.getInstance(this);
        helper.stop();
        for (Message message : adapter.getItems()) {
            if (message.body != null && !message.body.trim().isEmpty()) {
                helper.speakQueued(message.body);
            }
        }
    }

    @Override
    public void onVoiceFindChat(String query) {
        if (TextUtils.isEmpty(query)) {
            return;
        }
        List<Conversation> results = repository.searchConversations(query);
        if (results.isEmpty()) {
            SpeechHelper.getInstance(this).speak(query);
            return;
        }
        Conversation target = results.get(0);
        if (target.id != conversationId) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("conversation_id", target.id);
            startActivity(intent);
        }
        String name = target.displayName != null ? target.displayName : target.address;
        SpeechHelper.getInstance(this).speak(name);
    }

    @Override
    public void onVoiceGoBack() {
        finish();
    }

    @Override
    public void onVoiceDictation(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        composeField.setText(text);
        composeField.setSelection(text.length());
    }

    private void refreshMessages() {
        adapter.setItems(repository.getMessages(conversationId));
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (messageList == null || adapter == null) {
            return;
        }
        int count = adapter.getItemCount();
        if (count > 0) {
            messageList.post(() -> messageList.smoothScrollToPosition(count - 1));
        }
    }

    private void refreshAccessibilityUi() {
        if (adapter != null) {
            adapter.setShowSpeakButton(AccessibilitySettings.isSpeakButtonEnabled());
        }
        if (micBtn != null) {
            micBtn.setVisibility(AccessibilitySettings.isSttComposeEnabled() ? View.VISIBLE : View.GONE);
        }
        if (actionbar != null && repository != null && conversationId > 0) {
            initActionbar(actionbar);
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        super.didReceivedNotification(id, args);
        if (id == NotificationCenter.didReceiveSms && repository != null && conversationId > 0) {
            if (args.length > 0 && args[0] instanceof Long && (Long) args[0] != conversationId) {
                return;
            }
            refreshMessages();
        } else if (id == NotificationCenter.didAccessibilitySettingsChanged) {
            refreshAccessibilityUi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PersianTtsEngine.getInstance(this).setProgressActivity(this);
        if (repository != null && conversationId > 0) {
            repository.markConversationRead(conversationId);
        }
    }

    @Override
    protected void onPause() {
        PersianTtsEngine.getInstance(this).setProgressActivity(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (voiceController != null) {
            voiceController.stop();
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceiveSms);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didAccessibilitySettingsChanged);
        super.onDestroy();
    }
}

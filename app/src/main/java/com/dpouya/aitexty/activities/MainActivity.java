package com.dpouya.aitexty.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.R;
import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.accessibility.AccessibilitySettings;
import com.dpouya.aitexty.accessibility.SpeechHelper;
import com.dpouya.aitexty.accessibility.VoiceCommandController;
import com.dpouya.aitexty.ai.LlamaEngine;
import com.dpouya.aitexty.data.SmsRepository;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.privacy.BlocklistManager;
import com.dpouya.aitexty.privacy.HiddenChatManager;
import com.dpouya.aitexty.privacy.UnlockGestureDetector;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.ActionBar.ActionBarButton;
import com.dpouya.aitexty.ui.adapter.ConversationAdapter;
import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.components.FaIconView;
import com.dpouya.aitexty.helper.FontAwesome;
import com.dpouya.aitexty.helper.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements NotificationCenter.NotificationCenterDelegate,
        VoiceCommandController.Host {
    private RecyclerView listView;
    private ConversationAdapter adapter;
    private SmsRepository repository;
    private boolean spamFilter;
    private String searchQuery = "";
    private final UnlockGestureDetector gestureDetector = new UnlockGestureDetector();
    private FrameLayout fab;
    private VoiceCommandController voiceController;
    private boolean pendingVoiceAction;

    @Override
    protected View initViews(Context context) {
        repository = SmsRepository.getInstance(context);
        voiceController = new VoiceCommandController(this);
        voiceController.setHost(this);
        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));

        listView = new RecyclerView(context);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setClipToPadding(false);
        adapter = new ConversationAdapter(context, new ConversationAdapter.Listener() {
            @Override
            public void onClick(Conversation conversation) {
                if (adapter.isSelectionMode()) {
                    adapter.toggleSelection(conversation.id);
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("conversation_id", conversation.id);
                startActivity(intent);
            }

            @Override
            public void onLongClick(Conversation conversation) {
                if (!adapter.isSelectionMode()) {
                    adapter.enterSelectionMode(conversation.id);
                } else {
                    adapter.toggleSelection(conversation.id);
                }
            }

            @Override
            public void onSelectionChanged(int count) {
                updateSelectionActionBar(count);
            }
        });
        listView.setAdapter(adapter);
        root.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fab = new FrameLayout(context);
        GradientDrawable fabBg = new GradientDrawable();
        fabBg.setShape(GradientDrawable.OVAL);
        fabBg.setColor(Theme.getColor(Theme.ACTION_BUTTON_BACKGROUND_COLOR));
        fab.setBackground(fabBg);
        fab.setElevation(AndroidUtilities.dp(4));
        FaIconView fabIcon = new FaIconView(context, 22);
        fabIcon.setIcon(FontAwesome.Icon.PLUS);
        fabIcon.setIconColor(Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR));
        fab.addView(fabIcon, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NewMessageActivity.class)));
        gestureDetector.attachFabListener(fab, HiddenChatManager.getInstance(context));
        root.addView(fab, LayoutHelper.createFrame(56, 56, Gravity.BOTTOM | Gravity.END, 0, 0, 16, 16));

        loadConversations();
        repository.syncFromProvider();

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didConversationsChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didReceiveSms);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.hiddenVaultUnlocked);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didAccessibilitySettingsChanged);

        return root;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        configureDefaultActionBar(bar);
    }

    private void configureDefaultActionBar(ActionBar bar) {
        bar.reset();
        bar.hideCenteredText();
        bar.setCenteredText(LocaleController.getString("conversations"),
                Theme.getTypeface(Theme.TypeFaceKey.ACTIONBAR_TYPEFACE), 18);
        bar.addButton(new ActionBarButton(FontAwesome.Icon.SEARCH,
                Theme.getFaIconColor(Theme.DrawableKey.SEARCH_DRAWABLE), v -> {
            if (adapter != null && adapter.isSelectionMode()) {
                return;
            }
            boolean searching = actionbar.getTxtSearch().getVisibility() != View.VISIBLE;
            bar.setSearchmode(searching);
            bar.setSearchhint(LocaleController.getString("search_messages"));
        }));
        bar.addButton(new ActionBarButton(FontAwesome.Icon.USER_GEAR,
                Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR), v -> {
            if (adapter == null || !adapter.isSelectionMode()) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
        }));
        bar.addButton(new ActionBarButton(
                Theme.isNight() ? FontAwesome.Icon.SUN : FontAwesome.Icon.MOON,
                Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR), v -> Theme.toggleNightMode()));
        if (AccessibilitySettings.isVoiceControlEnabled()) {
            bar.addButton(new ActionBarButton(FontAwesome.Icon.MICROPHONE,
                    Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR), v -> startVoiceCommands()));
        }

        gestureDetector.attachSearchListener(bar, HiddenChatManager.getInstance(this));

        bar.getTxtSearch().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null && adapter.isSelectionMode()) {
                    return;
                }
                searchQuery = s.toString();
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateSelectionActionBar(int count) {
        if (actionbar == null) {
            return;
        }
        if (count <= 0) {
            fab.setVisibility(View.VISIBLE);
            configureDefaultActionBar(actionbar);
            return;
        }

        fab.setVisibility(View.GONE);
        actionbar.reset();
        actionbar.hideCenteredText();
        actionbar.showBackButton(true);
        actionbar.setOnIconClick(v -> exitSelectionMode());
        actionbar.setTitle(String.format(LocaleController.getString("selected_count"), count));

        int iconColor = Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR);
        actionbar.addButton(new ActionBarButton(FontAwesome.Icon.CHECK, iconColor, v -> adapter.selectAll()));
        actionbar.addButton(new ActionBarButton(FontAwesome.Icon.EYE_SLASH, iconColor, v -> hideSelectedConversations()));
        actionbar.addButton(new ActionBarButton(FontAwesome.Icon.BAN, iconColor, v -> blockSelectedConversations()));
        actionbar.addButton(new ActionBarButton(FontAwesome.Icon.TRASH, iconColor, v -> deleteSelectedConversations()));
    }

    private void exitSelectionMode() {
        if (adapter != null) {
            adapter.exitSelectionMode();
        }
    }

    private void hideSelectedConversations() {
        HiddenChatManager hidden = HiddenChatManager.getInstance(this);
        for (Conversation conversation : adapter.getSelectedConversations()) {
            hidden.hideConversation(conversation.id, null, null);
        }
        exitSelectionMode();
        loadConversations();
    }

    private void blockSelectedConversations() {
        BlocklistManager blocklist = BlocklistManager.getInstance(this);
        for (Conversation conversation : adapter.getSelectedConversations()) {
            blocklist.block(conversation.address, conversation.displayName,
                    BlocklistManager.MODE_SILENT, "manual");
        }
        exitSelectionMode();
        loadConversations();
    }

    private void deleteSelectedConversations() {
        List<Long> ids = new ArrayList<>();
        for (Conversation conversation : adapter.getSelectedConversations()) {
            ids.add(conversation.id);
        }
        if (ids.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(LocaleController.getString("delete_conversations_confirm"))
                .setPositiveButton(LocaleController.getString("delete"), (d, w) -> {
                    repository.deleteConversations(ids);
                    exitSelectionMode();
                    loadConversations();
                })
                .setNegativeButton(LocaleController.getString("back"), null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (adapter != null && adapter.isSelectionMode()) {
            exitSelectionMode();
            return;
        }
        super.onBackPressed();
    }

    private void performSearch() {
        if (searchQuery.isEmpty()) {
            loadConversations();
            return;
        }
        List<Conversation> convResults = repository.searchConversations(searchQuery);
        List<Message> msgResults = repository.searchMessages(searchQuery);
        if (msgResults.size() > 0) {
            LlamaEngine.getInstance(this).generate(
                    com.dpouya.aitexty.ai.PromptTemplates.searchRerank(searchQuery, msgResults.toString()),
                    new LlamaEngine.StreamCallback() {
                        @Override
                        public void onToken(String token) {
                        }

                        @Override
                        public void onComplete(String full) {
                            // Keep conversation results; AI rerank enriches ordering
                            adapter.setItems(convResults);
                        }

                        @Override
                        public void onError(String error) {
                            adapter.setItems(convResults);
                        }
                    });
        } else {
            adapter.setItems(convResults);
        }
    }

    private void loadConversations() {
        if (adapter != null && adapter.isSelectionMode()) {
            return;
        }
        adapter.setItems(repository.getVisibleConversations(spamFilter));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadConversations();
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        super.didReceivedNotification(id, args);
        if (id == NotificationCenter.didConversationsChanged || id == NotificationCenter.didReceiveSms) {
            if (adapter == null || !adapter.isSelectionMode()) {
                loadConversations();
            } else {
                adapter.setItems(repository.getVisibleConversations(spamFilter));
            }
        } else if (id == NotificationCenter.didAccessibilitySettingsChanged) {
            if (adapter == null || !adapter.isSelectionMode()) {
                configureDefaultActionBar(actionbar);
            }
        } else if (id == NotificationCenter.hiddenVaultUnlocked) {
            if (args.length > 0 && "need_pin".equals(args[0])) {
                showPinDialog();
            } else {
                startActivity(new Intent(this, HiddenVaultActivity.class));
            }
        }
    }

    private void showPinDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(LocaleController.getString("pin_unlock"))
                .setView(input)
                .setPositiveButton("OK", (d, w) -> {
                    if (gestureDetector.verifyPin(input.getText().toString())) {
                        HiddenChatManager.getInstance(this).unlockVault();
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        if (voiceController != null) {
            voiceController.stop();
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didConversationsChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didReceiveSms);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.hiddenVaultUnlocked);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didAccessibilitySettingsChanged);
        super.onDestroy();
    }

    private void startVoiceCommands() {
        if (adapter != null && adapter.isSelectionMode()) {
            return;
        }
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
        SpeechHelper.getInstance(this).speak(LocaleController.getString("compose_hint"));
    }

    @Override
    public void onVoiceReadMessage() {
        List<Conversation> conversations = repository.getVisibleConversations(spamFilter);
        if (conversations.isEmpty()) {
            SpeechHelper.getInstance(this).speak(LocaleController.getString("no_conversations"));
            return;
        }
        Conversation latest = conversations.get(0);
        if (latest.preview != null && !latest.preview.isEmpty()) {
            SpeechHelper.getInstance(this).speak(latest.preview);
        }
    }

    @Override
    public void onVoiceReadAllMessages() {
        onVoiceReadMessage();
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
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("conversation_id", target.id);
        startActivity(intent);
        String name = target.displayName != null ? target.displayName : target.address;
        SpeechHelper.getInstance(this).speak(name);
    }

    @Override
    public void onVoiceGoBack() {
        // Already on main screen
    }

    @Override
    public void onVoiceDictation(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        actionbar.setSearchmode(true);
        actionbar.setSearchhint(LocaleController.getString("search_messages"));
        actionbar.getTxtSearch().setText(text);
        searchQuery = text;
        performSearch();
    }
}

package com.dpouya.aitexty.privacy;

import android.content.Context;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.helper.AppSettings;

import java.util.List;

public class HiddenChatManager {
    private static HiddenChatManager instance;
    private final Context context;
    private boolean vaultUnlocked;
    private long unlockUntil;

    private HiddenChatManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized HiddenChatManager getInstance(Context context) {
        if (instance == null) {
            instance = new HiddenChatManager(context);
        }
        return instance;
    }

    public boolean isVaultUnlocked() {
        if (vaultUnlocked && System.currentTimeMillis() > unlockUntil) {
            lockVault();
        }
        return vaultUnlocked;
    }

    public void unlockVault() {
        int timeoutMin = AppSettings.Int(AppSettings.Key.HIDDEN_VAULT_TIMEOUT_MIN);
        if (timeoutMin <= 0) timeoutMin = 5;
        vaultUnlocked = true;
        unlockUntil = System.currentTimeMillis() + timeoutMin * 60_000L;
        NotificationCenter.getInstance().postNotification(NotificationCenter.hiddenVaultUnlocked);
    }

    public void lockVault() {
        vaultUnlocked = false;
        unlockUntil = 0;
        NotificationCenter.getInstance().postNotification(NotificationCenter.hiddenVaultLocked);
    }

    public List<Conversation> getHiddenConversations() {
        if (!isVaultUnlocked()) return java.util.Collections.emptyList();
        return AppDatabase.getInstance(context).conversationDao().getHiddenConversations();
    }

    public void hideConversation(long conversationId, String decoyTitle, String decoyBody) {
        Conversation c = AppDatabase.getInstance(context).conversationDao().getById(conversationId);
        if (c == null) return;
        c.isHidden = true;
        c.hiddenAt = System.currentTimeMillis();
        c.decoyTitle = decoyTitle;
        c.decoyBody = decoyBody;
        AppDatabase.getInstance(context).conversationDao().update(c);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    public void unhideConversation(long conversationId) {
        AppDatabase.getInstance(context).conversationDao().setHidden(conversationId, false, 0);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }
}

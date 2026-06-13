package com.dpouya.aitexty.privacy;

import android.view.View;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;

public class UnlockGestureDetector {
    private int searchTapCount;
    private long lastSearchTap;
    private long fabPressStart;

    public void onSearchIconTap(HiddenChatManager vault) {
        long now = System.currentTimeMillis();
        if (now - lastSearchTap > 2000) {
            searchTapCount = 0;
        }
        lastSearchTap = now;
        searchTapCount++;
        if (searchTapCount >= 3) {
            searchTapCount = 0;
            tryUnlock(vault);
        }
    }

    public void onFabTouchDown() {
        fabPressStart = System.currentTimeMillis();
    }

    public void onFabTouchUp(HiddenChatManager vault) {
        if (fabPressStart > 0 && System.currentTimeMillis() - fabPressStart >= 3000) {
            tryUnlock(vault);
        }
        fabPressStart = 0;
    }

    public boolean verifyPin(String input) {
        String stored = AppSettings.String(AppSettings.Key.HIDDEN_VAULT_PIN_HASH);
        if (stored == null) stored = "";
        if (stored.isEmpty()) return false;
        return stored.equals(hashPin(input));
    }

    public void setPin(String pin) {
        AppSettings.String(AppSettings.Key.HIDDEN_VAULT_PIN_HASH, hashPin(pin));
    }

    private void tryUnlock(HiddenChatManager vault) {
        String method = AppSettings.String(AppSettings.Key.HIDDEN_UNLOCK_METHOD);
        if (method == null || method.isEmpty()) method = "gesture";
        if ("pin".equals(method)) {
            NotificationCenter.getInstance().postNotification(NotificationCenter.hiddenVaultUnlocked, "need_pin");
        } else {
            vault.unlockVault();
        }
    }

    private String hashPin(String pin) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            return pin;
        }
    }

    public void attachSearchListener(ActionBar actionBar, HiddenChatManager vault) {
        if (actionBar.getSearchIcon() != null) {
            actionBar.getSearchIcon().setOnClickListener(v -> onSearchIconTap(vault));
        }
    }

    public void attachFabListener(View fab, HiddenChatManager vault) {
        fab.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                onFabTouchDown();
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                onFabTouchUp(vault);
            }
            return false;
        });
    }
}

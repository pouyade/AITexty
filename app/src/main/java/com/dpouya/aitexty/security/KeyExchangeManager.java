package com.dpouya.aitexty.security;

import android.content.Context;

import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.security.CryptoManager;

public class KeyExchangeManager {
    private final CryptoManager cryptoManager;

    public KeyExchangeManager(Context context) {
        cryptoManager = CryptoManager.getInstance(context);
    }

    public String createQrPayload(long conversationId) throws Exception {
        return cryptoManager.generateQrPayload(conversationId);
    }

    public void scanAndExchange(long conversationId, String qrJson) throws Exception {
        cryptoManager.completeKeyExchange(conversationId, qrJson);
    }
}

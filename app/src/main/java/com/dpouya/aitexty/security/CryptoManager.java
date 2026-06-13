package com.dpouya.aitexty.security;

import android.content.Context;
import android.util.Base64;

import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.EncryptionSession;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoManager {
    public static final String E2E_PREFIX = "AITXT:E:";
    public static final String HS_PREFIX = "AITXT:HS:";

    private static CryptoManager instance;
    private final Context context;
    private final Map<Long, byte[]> conversationKeys = new HashMap<>();
    private final Map<Long, byte[]> e2eKeys = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    private CryptoManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized CryptoManager getInstance(Context context) {
        if (instance == null) {
            instance = new CryptoManager(context);
        }
        return instance;
    }

    public void enableLocalEncryption(long conversationId) {
        byte[] key = new byte[32];
        random.nextBytes(key);
        conversationKeys.put(conversationId, key);
        Conversation conv = AppDatabase.getInstance(context).conversationDao().getById(conversationId);
        if (conv != null) {
            conv.isEncrypted = true;
            AppDatabase.getInstance(context).conversationDao().update(conv);
        }
    }

    public String encryptForConversation(long conversationId, String plaintext) {
        byte[] key = getOrCreateKey(conversationId);
        return encrypt(key, plaintext);
    }

    public String decryptForConversation(long conversationId, String ciphertext) {
        byte[] key = conversationKeys.get(conversationId);
        if (key == null) return "[encrypted]";
        return decrypt(key, ciphertext);
    }

    private byte[] getOrCreateKey(long conversationId) {
        byte[] key = conversationKeys.get(conversationId);
        if (key == null) {
            key = new byte[32];
            random.nextBytes(key);
            conversationKeys.put(conversationId, key);
        }
        return key;
    }

    public String generateQrPayload(long conversationId) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair kp = kpg.generateKeyPair();
        String pub = Base64.encodeToString(kp.getPublic().getEncoded(), Base64.NO_WRAP);
        String sessionId = Base64.encodeToString(random.generateSeed(16), Base64.NO_WRAP);

        EncryptionSession session = new EncryptionSession();
        session.conversationId = conversationId;
        session.sessionId = sessionId;
        session.publicKey = pub;
        session.e2eActive = false;
        session.createdAt = System.currentTimeMillis();
        AppDatabase.getInstance(context).encryptionSessionDao().insert(session);

        return "{\"pub\":\"" + pub + "\",\"sid\":\"" + sessionId + "\",\"v\":1}";
    }

    public void completeKeyExchange(long conversationId, String qrJson) throws Exception {
        org.json.JSONObject obj = new org.json.JSONObject(qrJson);
        String remotePub = obj.getString("pub");
        byte[] shared = deriveSharedSecret(remotePub);
        e2eKeys.put(conversationId, shared);

        EncryptionSession session = AppDatabase.getInstance(context).encryptionSessionDao().getByConversationId(conversationId);
        if (session != null) {
            session.e2eActive = true;
            session.sharedSecretHash = hashBytes(shared);
            AppDatabase.getInstance(context).encryptionSessionDao().insert(session);
        }

        Conversation conv = AppDatabase.getInstance(context).conversationDao().getById(conversationId);
        if (conv != null) {
            conv.e2eEnabled = true;
            AppDatabase.getInstance(context).conversationDao().update(conv);
        }
    }

    public String encryptE2E(long conversationId, String plaintext) {
        byte[] key = e2eKeys.get(conversationId);
        if (key == null) key = getOrCreateKey(conversationId);
        return E2E_PREFIX + encrypt(key, plaintext);
    }

    public String decryptE2E(long conversationId, String payload) {
        if (!payload.startsWith(E2E_PREFIX)) return payload;
        byte[] key = e2eKeys.get(conversationId);
        if (key == null) return "[e2e encrypted]";
        return decrypt(key, payload.substring(E2E_PREFIX.length()));
    }

    private byte[] deriveSharedSecret(String remotePubB64) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        KeyPair local = kpg.generateKeyPair();
        byte[] remotePub = Base64.decode(remotePubB64, Base64.NO_WRAP);
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(local.getPrivate());
        ka.doPhase(java.security.KeyFactory.getInstance("EC").generatePublic(
                new java.security.spec.X509EncodedKeySpec(remotePub)), true);
        return MessageDigest.getInstance("SHA-256").digest(ka.generateSecret());
    }

    private String encrypt(byte[] key, String plaintext) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.encodeToString(combined, Base64.NO_WRAP);
        } catch (Exception e) {
            return plaintext;
        }
    }

    private String decrypt(byte[] key, String ciphertext) {
        try {
            byte[] combined = Base64.decode(ciphertext, Base64.NO_WRAP);
            byte[] iv = new byte[12];
            System.arraycopy(combined, 0, iv, 0, 12);
            byte[] encrypted = new byte[combined.length - 12];
            System.arraycopy(combined, 12, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "[encrypted]";
        }
    }

    private String hashBytes(byte[] data) throws Exception {
        return Base64.encodeToString(MessageDigest.getInstance("SHA-256").digest(data), Base64.NO_WRAP);
    }
}

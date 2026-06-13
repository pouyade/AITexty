package com.dpouya.aitexty.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.data.dao.ConversationDao;
import com.dpouya.aitexty.data.dao.MessageDao;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.privacy.BlocklistManager;
import com.dpouya.aitexty.security.CryptoManager;
import com.dpouya.aitexty.helper.ContactHelper;
import com.dpouya.aitexty.helper.LocaleController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsRepository {
    private static SmsRepository instance;
    private final Context context;
    private final AppDatabase db;
    private final ConversationDao conversationDao;
    private final MessageDao messageDao;
    private final CryptoManager cryptoManager;

    private SmsRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(context);
        this.conversationDao = db.conversationDao();
        this.messageDao = db.messageDao();
        this.cryptoManager = CryptoManager.getInstance(context);
    }

    public static synchronized SmsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SmsRepository(context);
        }
        return instance;
    }

    public AppDatabase getDatabase() {
        return db;
    }

    public List<Conversation> getVisibleConversations(boolean spamOnly) {
        if (spamOnly) {
            return conversationDao.getSpamConversations();
        }
        return conversationDao.getVisibleConversations();
    }

    public List<Conversation> getHiddenConversations() {
        return conversationDao.getHiddenConversations();
    }

    public Conversation getConversation(long id) {
        return conversationDao.getById(id);
    }

    public Conversation getOrCreateConversation(String address, String threadId) {
        Conversation c = conversationDao.getByAddress(address);
        if (c == null) {
            c = new Conversation();
            c.address = address;
            c.threadId = threadId != null ? threadId : address;
            c.displayName = ContactHelper.resolveDisplayName(context, address);
            c.preview = "";
            c.lastTimestamp = 0;
            c.unreadCount = 0;
            c.id = conversationDao.insert(c);
        }
        return c;
    }

    public List<Message> getMessages(long conversationId) {
        List<Message> messages = messageDao.getByConversation(conversationId);
        Conversation conv = conversationDao.getById(conversationId);
        if (conv != null && conv.isEncrypted) {
            for (Message m : messages) {
                if (m.isEncrypted && !TextUtils.isEmpty(m.encryptedBody)) {
                    m.body = cryptoManager.decryptForConversation(conversationId, m.encryptedBody);
                }
            }
        }
        return messages;
    }

    public Message insertIncomingMessage(String address, String body, long timestamp) {
        if (BlocklistManager.getInstance(context).isBlocked(address)) {
            return null;
        }
        Conversation conv = getOrCreateConversation(address, address);
        long msgTimestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();

        String storeBody = body;
        if (body != null && body.startsWith(CryptoManager.E2E_PREFIX)) {
            storeBody = cryptoManager.decryptE2E(conv.id, body);
            conv.e2eEnabled = true;
        }

        Message duplicate = messageDao.findByFingerprint(conv.id, msgTimestamp, storeBody != null ? storeBody : "");
        if (duplicate != null) {
            return duplicate;
        }

        String systemId = writeToTelephonyInbox(address, body, msgTimestamp);
        if (systemId != null) {
            Message bySystemId = messageDao.getBySystemId(systemId);
            if (bySystemId != null) {
                return bySystemId;
            }
        }

        Message msg = new Message();
        msg.conversationId = conv.id;
        msg.threadId = conv.threadId;
        msg.address = address;
        msg.timestamp = msgTimestamp;
        msg.type = Telephony.Sms.MESSAGE_TYPE_INBOX;
        msg.status = Telephony.Sms.STATUS_COMPLETE;
        msg.systemId = systemId;

        if (body != null && body.startsWith(CryptoManager.E2E_PREFIX)) {
            msg.body = storeBody;
            msg.isEncrypted = true;
        } else if (conv.isEncrypted) {
            msg.encryptedBody = cryptoManager.encryptForConversation(conv.id, body);
            msg.body = body;
            msg.isEncrypted = true;
        } else {
            msg.body = body;
        }

        msg.id = messageDao.insert(msg);
        updateConversationPreview(conv, msg);
        notifyMessageReceived(conv.id);
        return msg;
    }

    private void notifyMessageReceived(long conversationId) {
        NotificationCenter.getInstance().postNotification(NotificationCenter.didReceiveSms, conversationId);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    private String writeToTelephonyInbox(String address, String body, long timestamp) {
        try {
            ContentValues values = new ContentValues();
            values.put(Telephony.Sms.ADDRESS, address);
            values.put(Telephony.Sms.BODY, body);
            values.put(Telephony.Sms.DATE, timestamp);
            values.put(Telephony.Sms.READ, 0);
            values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX);
            Uri uri = context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, values);
            return uri != null ? uri.getLastPathSegment() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String writeToTelephonySent(String address, String body, long timestamp) {
        try {
            ContentValues values = new ContentValues();
            values.put(Telephony.Sms.ADDRESS, address);
            values.put(Telephony.Sms.BODY, body);
            values.put(Telephony.Sms.DATE, timestamp);
            values.put(Telephony.Sms.READ, 1);
            values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT);
            Uri uri = context.getContentResolver().insert(Telephony.Sms.Sent.CONTENT_URI, values);
            return uri != null ? uri.getLastPathSegment() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateConversationPreview(Conversation conv, Message msg) {
        String preview = conv.isEncrypted
                ? LocaleController.getString("encrypted_message")
                : (msg.body != null ? msg.body.replace('\n', ' ').trim() : "");
        conv.preview = preview;
        conv.lastTimestamp = msg.timestamp;
        conv.unreadCount = conv.unreadCount + 1;
        conversationDao.updatePreview(conv.id, preview, msg.timestamp, conv.unreadCount);
        conversationDao.update(conv);
    }

    public Message sendMessage(long conversationId, String body) {
        Conversation conv = conversationDao.getById(conversationId);
        if (conv == null) return null;

        String sendBody = body;
        if (conv.e2eEnabled) {
            sendBody = cryptoManager.encryptE2E(conversationId, body);
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(conv.address, null, sendBody, null, null);

        long msgTimestamp = System.currentTimeMillis();

        String systemId = writeToTelephonySent(conv.address, sendBody, msgTimestamp);
        if (systemId != null) {
            Message bySystemId = messageDao.getBySystemId(systemId);
            if (bySystemId != null) {
                return bySystemId;
            }
        }

        Message msg = new Message();
        msg.conversationId = conversationId;
        msg.threadId = conv.threadId;
        msg.address = conv.address;
        msg.timestamp = msgTimestamp;
        msg.type = Telephony.Sms.MESSAGE_TYPE_SENT;
        msg.status = Telephony.Sms.STATUS_COMPLETE;
        msg.systemId = systemId;

        if (conv.isEncrypted) {
            msg.encryptedBody = cryptoManager.encryptForConversation(conversationId, body);
            msg.body = body;
            msg.isEncrypted = true;
        } else {
            msg.body = body;
        }

        msg.id = messageDao.insert(msg);
        String preview = conv.isEncrypted
                ? LocaleController.getString("encrypted_message")
                : (body != null ? body.replace('\n', ' ').trim() : "");
        conversationDao.updatePreview(conversationId, preview, msg.timestamp, 0);
        notifyMessageReceived(conversationId);
        return msg;
    }

    public void markConversationRead(long conversationId) {
        Conversation conv = conversationDao.getById(conversationId);
        if (conv == null || conv.unreadCount == 0) {
            return;
        }
        conv.unreadCount = 0;
        conversationDao.clearUnread(conversationId);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    public void syncFromProvider() {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Telephony.Sms.CONTENT_URI;
        Cursor cursor = resolver.query(uri, null, null, null, Telephony.Sms.DATE + " DESC LIMIT 200");
        if (cursor == null) return;

        Map<String, Conversation> threadMap = new HashMap<>();
        Map<String, Boolean> previewUpdated = new HashMap<>();
        try {
            while (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE));
                String threadId = String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)));

                Conversation conv = threadMap.get(address);
                if (conv == null) {
                    conv = getOrCreateConversation(address, threadId);
                    threadMap.put(address, conv);
                }

                // Cursor is newest-first; first row per address is the latest message.
                if (!previewUpdated.containsKey(address)) {
                    previewUpdated.put(address, true);
                    String previewBody = formatPreviewText(body, conv);
                    conv.preview = previewBody;
                    conv.lastTimestamp = date;
                    if (conv.displayName == null || conv.displayName.equals(conv.address)) {
                        conv.displayName = ContactHelper.resolveDisplayName(context, address);
                    }
                    conversationDao.updatePreview(conv.id, previewBody, date, conv.unreadCount);
                    conversationDao.update(conv);
                }

                String systemId = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms._ID));
                Message existing = messageDao.getBySystemId(systemId);
                if (existing == null) {
                    Message dup = messageDao.findByFingerprint(conv.id, date, body != null ? body : "");
                    if (dup != null) {
                        if (dup.systemId == null || dup.systemId.isEmpty()) {
                            dup.systemId = systemId;
                            messageDao.insert(dup);
                        }
                        continue;
                    }
                    Message msg = new Message();
                    msg.conversationId = conv.id;
                    msg.threadId = threadId;
                    msg.address = address;
                    msg.body = body;
                    msg.timestamp = date;
                    msg.type = type;
                    msg.systemId = systemId;
                    messageDao.insert(msg);
                }
            }
        } finally {
            cursor.close();
        }
        refreshAllPreviews();
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    private String formatPreviewText(String body, Conversation conv) {
        if (conv != null && conv.isEncrypted) {
            return LocaleController.getString("encrypted_message");
        }
        if (body == null) return "";
        return body.replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    public void refreshAllPreviews() {
        List<Conversation> conversations = conversationDao.getVisibleConversations();
        for (Conversation conv : conversations) {
            Message latest = messageDao.getLatestMessage(conv.id);
            if (latest == null) continue;
            String preview = conv.isEncrypted
                    ? LocaleController.getString("encrypted_message")
                    : formatPreviewText(latest.body, conv);
            if (preview.isEmpty()) continue;
            conversationDao.updatePreview(conv.id, preview, latest.timestamp, conv.unreadCount);
        }
    }

    public List<Message> searchMessages(String query) {
        if (TextUtils.isEmpty(query)) return new ArrayList<>();
        return messageDao.search(query);
    }

    public List<Conversation> searchConversations(String query) {
        List<Conversation> all = conversationDao.getVisibleConversations();
        List<Conversation> result = new ArrayList<>();
        String q = query.toLowerCase();
        for (Conversation c : all) {
            if ((c.displayName != null && c.displayName.toLowerCase().contains(q))
                    || (c.address != null && c.address.contains(q))
                    || (c.preview != null && c.preview.toLowerCase().contains(q))) {
                result.add(c);
            }
        }
        return result;
    }

    public void hideConversation(long id, boolean hidden) {
        conversationDao.setHidden(id, hidden, hidden ? System.currentTimeMillis() : 0);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    public void markSpam(long id, boolean spam) {
        conversationDao.setSpam(id, spam);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    public void deleteConversations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            if (id == null) continue;
            Conversation conv = conversationDao.getById(id);
            if (conv == null) continue;
            deleteFromTelephony(conv.address);
            messageDao.deleteByConversation(id);
            conversationDao.deleteById(id);
        }
        NotificationCenter.getInstance().postNotification(NotificationCenter.didConversationsChanged);
    }

    private void deleteFromTelephony(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        try {
            context.getContentResolver().delete(
                    Telephony.Sms.CONTENT_URI,
                    Telephony.Sms.ADDRESS + " = ?",
                    new String[]{address});
        } catch (Exception ignored) {
        }
    }
}

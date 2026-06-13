package com.dpouya.aitexty.privacy;

import android.content.Context;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.BlockEntry;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.Message;

public class BlocklistManager {
    public static final String MODE_SILENT = "SILENT";
    public static final String MODE_REJECT = "REJECT";
    public static final String MODE_DELETE = "DELETE";

    private static BlocklistManager instance;
    private final Context context;
    private final AppDatabase db;

    private BlocklistManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(context);
    }

    public static synchronized BlocklistManager getInstance(Context context) {
        if (instance == null) {
            instance = new BlocklistManager(context);
        }
        return instance;
    }

    public boolean isBlocked(String phone) {
        if (phone == null) return false;
        return db.blockEntryDao().isBlocked(normalize(phone)) > 0;
    }

    public BlockEntry getBlockEntry(String phone) {
        return db.blockEntryDao().getByPhone(normalize(phone));
    }

    public void block(String phone, String displayName, String mode, String reason) {
        BlockEntry entry = new BlockEntry();
        entry.phoneNumber = normalize(phone);
        entry.displayName = displayName != null ? displayName : phone;
        entry.blockMode = mode != null ? mode : MODE_SILENT;
        entry.reason = reason;
        entry.blockedAt = System.currentTimeMillis();
        entry.syncWithSpam = false;
        db.blockEntryDao().insert(entry);
        NotificationCenter.getInstance().postNotification(NotificationCenter.didBlocklistChanged);
    }

    public void unblock(String phone) {
        db.blockEntryDao().deleteByPhone(normalize(phone));
        NotificationCenter.getInstance().postNotification(NotificationCenter.didBlocklistChanged);
    }

    public java.util.List<BlockEntry> getAll() {
        return db.blockEntryDao().getAll();
    }

    public boolean handleIncoming(Message msg, String address) {
        BlockEntry entry = getBlockEntry(address);
        if (entry == null) return false;
        if (MODE_DELETE.equals(entry.blockMode) && msg != null) {
            db.messageDao().deleteById(msg.id);
        }
        return true;
    }

    private String normalize(String phone) {
        return phone.replaceAll("[^0-9+]", "");
    }
}

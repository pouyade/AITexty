package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversations")
public class Conversation {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String threadId;
    public String address;
    public String displayName;
    public String preview;
    public long lastTimestamp;
    public int unreadCount;
    public boolean isSpam;
    public boolean isHidden;
    public long hiddenAt;
    public String decoyTitle;
    public String decoyBody;
    public String decoyIconStyle;
    public boolean isEncrypted;
    public boolean e2eEnabled;
    public String encryptionKeyId;
    public int filterTab; // 0=all, 1=spam
}

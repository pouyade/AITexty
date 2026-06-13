package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "encryption_sessions")
public class EncryptionSession {
    @PrimaryKey
    public long conversationId;
    public String sessionId;
    public String publicKey;
    public String sharedSecretHash;
    public boolean e2eActive;
    public long createdAt;
}

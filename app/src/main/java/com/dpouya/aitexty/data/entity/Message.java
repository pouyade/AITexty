package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages", indices = {
        @Index("conversationId"),
        @Index("timestamp"),
        @Index(value = "systemId", unique = true)
})
public class Message {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long conversationId;
    public String threadId;
    public String address;
    public String body;
    public String encryptedBody;
    public long timestamp;
    public int type; // 1=inbox, 2=sent
    public int status; // delivery status
    public boolean isEncrypted;
    public String systemId;
}

package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "block_entries", indices = {@Index(value = "phoneNumber", unique = true)})
public class BlockEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String phoneNumber;
    public String displayName;
    public long blockedAt;
    public String reason;
    public String blockMode; // SILENT, REJECT, DELETE
    public boolean syncWithSpam;
}

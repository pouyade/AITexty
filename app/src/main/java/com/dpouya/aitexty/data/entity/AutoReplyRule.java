package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "auto_reply_rules")
public class AutoReplyRule {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String address;
    public boolean enabled;
    public String persona;
    public String quietHoursStart;
    public String quietHoursEnd;
}

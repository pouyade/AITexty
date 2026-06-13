package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "spam_scores")
public class SpamScore {
    @PrimaryKey
    public long messageId;
    public String classification; // SPAM, HAM, UNCERTAIN
    public float confidence;
    public long classifiedAt;
}

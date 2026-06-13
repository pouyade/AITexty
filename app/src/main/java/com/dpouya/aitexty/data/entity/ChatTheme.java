package com.dpouya.aitexty.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_themes")
public class ChatTheme {
    @PrimaryKey
    public long conversationId;
    public String themeName;
    public String backgroundType; // color, gradient, image
    public String backgroundValue;
    public String bubbleIncomingColor;
    public String bubbleOutgoingColor;
    public float fontScale;
}

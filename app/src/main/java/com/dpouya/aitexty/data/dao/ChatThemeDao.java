package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dpouya.aitexty.data.entity.ChatTheme;

@Dao
public interface ChatThemeDao {
    @Query("SELECT * FROM chat_themes WHERE conversationId = :conversationId LIMIT 1")
    ChatTheme getByConversationId(long conversationId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatTheme theme);
}

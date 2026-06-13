package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dpouya.aitexty.data.entity.EncryptionSession;

@Dao
public interface EncryptionSessionDao {
    @Query("SELECT * FROM encryption_sessions WHERE conversationId = :conversationId LIMIT 1")
    EncryptionSession getByConversationId(long conversationId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EncryptionSession session);
}

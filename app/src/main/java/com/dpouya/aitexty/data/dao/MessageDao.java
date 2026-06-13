package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dpouya.aitexty.data.entity.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    List<Message> getByConversation(long conversationId);

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    Message getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Message message);

    @Query("DELETE FROM messages WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    void deleteByConversation(long conversationId);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    Message getLatestMessage(long conversationId);

    @Query("SELECT * FROM messages WHERE systemId = :systemId LIMIT 1")
    Message getBySystemId(String systemId);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND timestamp = :timestamp AND body = :body LIMIT 1")
    Message findByFingerprint(long conversationId, long timestamp, String body);

    @Query("SELECT * FROM messages WHERE body LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 50")
    List<Message> search(String query);
}

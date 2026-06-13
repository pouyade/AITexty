package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dpouya.aitexty.data.entity.Conversation;

import java.util.List;

@Dao
public interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isHidden = 0 ORDER BY lastTimestamp DESC")
    List<Conversation> getVisibleConversations();

    @Query("SELECT * FROM conversations WHERE isHidden = 1 ORDER BY lastTimestamp DESC")
    List<Conversation> getHiddenConversations();

    @Query("SELECT * FROM conversations WHERE isSpam = 1 AND isHidden = 0 ORDER BY lastTimestamp DESC")
    List<Conversation> getSpamConversations();

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    Conversation getById(long id);

    @Query("SELECT * FROM conversations WHERE address = :address LIMIT 1")
    Conversation getByAddress(String address);

    @Query("SELECT * FROM conversations WHERE threadId = :threadId LIMIT 1")
    Conversation getByThreadId(String threadId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Conversation conversation);

    @Update
    void update(Conversation conversation);

    @Query("UPDATE conversations SET isHidden = :hidden, hiddenAt = :hiddenAt WHERE id = :id")
    void setHidden(long id, boolean hidden, long hiddenAt);

    @Query("UPDATE conversations SET isSpam = :spam WHERE id = :id")
    void setSpam(long id, boolean spam);

    @Query("UPDATE conversations SET preview = :preview, lastTimestamp = :ts, unreadCount = :unread WHERE id = :id")
    void updatePreview(long id, String preview, long ts, int unread);

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    void clearUnread(long id);

    @Query("DELETE FROM conversations WHERE id = :id")
    void deleteById(long id);
}

package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dpouya.aitexty.data.entity.SpamScore;

@Dao
public interface SpamScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SpamScore score);

    @Query("SELECT * FROM spam_scores WHERE messageId = :messageId LIMIT 1")
    SpamScore getByMessageId(long messageId);
}

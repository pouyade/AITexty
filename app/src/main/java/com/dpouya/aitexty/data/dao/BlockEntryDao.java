package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dpouya.aitexty.data.entity.BlockEntry;

import java.util.List;

@Dao
public interface BlockEntryDao {
    @Query("SELECT * FROM block_entries ORDER BY blockedAt DESC")
    List<BlockEntry> getAll();

    @Query("SELECT * FROM block_entries WHERE phoneNumber = :phone LIMIT 1")
    BlockEntry getByPhone(String phone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BlockEntry entry);

    @Query("DELETE FROM block_entries WHERE phoneNumber = :phone")
    void deleteByPhone(String phone);

    @Query("SELECT COUNT(*) FROM block_entries WHERE phoneNumber = :phone")
    int isBlocked(String phone);
}

package com.dpouya.aitexty.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dpouya.aitexty.data.entity.AutoReplyRule;

@Dao
public interface AutoReplyRuleDao {
    @Query("SELECT * FROM auto_reply_rules WHERE address = :address LIMIT 1")
    AutoReplyRule getByAddress(String address);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AutoReplyRule rule);
}

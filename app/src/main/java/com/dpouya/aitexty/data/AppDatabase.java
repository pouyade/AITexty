package com.dpouya.aitexty.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dpouya.aitexty.data.dao.AutoReplyRuleDao;
import com.dpouya.aitexty.data.dao.BlockEntryDao;
import com.dpouya.aitexty.data.dao.ChatThemeDao;
import com.dpouya.aitexty.data.dao.ConversationDao;
import com.dpouya.aitexty.data.dao.EncryptionSessionDao;
import com.dpouya.aitexty.data.dao.MessageDao;
import com.dpouya.aitexty.data.dao.SpamScoreDao;
import com.dpouya.aitexty.data.entity.AutoReplyRule;
import com.dpouya.aitexty.data.entity.BlockEntry;
import com.dpouya.aitexty.data.entity.ChatTheme;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.EncryptionSession;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.data.entity.SpamScore;

@Database(entities = {
        Conversation.class, Message.class, SpamScore.class, AutoReplyRule.class,
        ChatTheme.class, BlockEntry.class, EncryptionSession.class
}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract SpamScoreDao spamScoreDao();
    public abstract AutoReplyRuleDao autoReplyRuleDao();
    public abstract ChatThemeDao chatThemeDao();
    public abstract BlockEntryDao blockEntryDao();
    public abstract EncryptionSessionDao encryptionSessionDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "aitexty.db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}

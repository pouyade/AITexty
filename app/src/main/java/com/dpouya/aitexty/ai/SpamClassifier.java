package com.dpouya.aitexty.ai;

import android.content.Context;

import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.data.entity.SpamScore;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.privacy.BlocklistManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpamClassifier {
    private static SpamClassifier instance;
    private final Context context;
    private final LlamaEngine engine;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SpamClassifier(Context context) {
        this.context = context.getApplicationContext();
        this.engine = LlamaEngine.getInstance(context);
    }

    public static synchronized SpamClassifier getInstance(Context context) {
        if (instance == null) {
            instance = new SpamClassifier(context);
        }
        return instance;
    }

    public void classifyAsync(Message message, String sender, String body) {
        executor.execute(() -> {
            String prompt = PromptTemplates.spamClassification(sender, body);
            engine.generate(prompt, new LlamaEngine.StreamCallback() {
                @Override
                public void onToken(String token) {
                }

                @Override
                public void onComplete(String full) {
                    String classification = parseClassification(full);
                    SpamScore score = new SpamScore();
                    score.messageId = message.id;
                    score.classification = classification;
                    score.confidence = "SPAM".equals(classification) ? 0.9f : ("HAM".equals(classification) ? 0.85f : 0.5f);
                    score.classifiedAt = System.currentTimeMillis();
                    AppDatabase.getInstance(context).spamScoreDao().insert(score);

                    if ("SPAM".equals(classification)) {
                        AppDatabase.getInstance(context).conversationDao().setSpam(message.conversationId, true);
                    }
                    if (AppSettings.Bool(AppSettings.Key.SPAM_AUTO_BLOCK) && "SPAM".equals(classification)) {
                        BlocklistManager.getInstance(context).block(sender, sender, BlocklistManager.MODE_SILENT, "AI spam");
                    }
                }

                @Override
                public void onError(String error) {
                }
            });
        });
    }

    private String parseClassification(String full) {
        if (full == null) return "UNCERTAIN";
        String upper = full.trim().toUpperCase();
        if (upper.contains("SPAM")) return "SPAM";
        if (upper.contains("HAM")) return "HAM";
        return "UNCERTAIN";
    }
}

package com.dpouya.aitexty.ai;

import android.content.Context;

import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.AutoReplyRule;
import com.dpouya.aitexty.data.entity.Message;

import java.util.List;

public class AutoResponseAgent {
    private final Context context;
    private final LlamaEngine engine;

    public AutoResponseAgent(Context context) {
        this.context = context.getApplicationContext();
        this.engine = LlamaEngine.getInstance(context);
    }

    public void suggestReply(long conversationId, LlamaEngine.StreamCallback callback) {
        List<Message> messages = AppDatabase.getInstance(context).messageDao().getByConversation(conversationId);
        StringBuilder history = new StringBuilder();
        int start = Math.max(0, messages.size() - 10);
        for (int i = start; i < messages.size(); i++) {
            Message m = messages.get(i);
            history.append(m.type == 1 ? "Them: " : "Me: ").append(m.body).append("\n");
        }
        AutoReplyRule rule = AppDatabase.getInstance(context).autoReplyRuleDao()
                .getByAddress(messages.isEmpty() ? "" : messages.get(messages.size() - 1).address);
        String persona = rule != null && rule.persona != null ? rule.persona : "friendly and helpful";
        String prompt = PromptTemplates.autoReply(history.toString(), persona);
        engine.generate(prompt, callback);
    }

    public void sendAutoReplyIfEnabled(long conversationId, Runnable onSent) {
        AutoReplyRule rule = getRuleForConversation(conversationId);
        if (rule == null || !rule.enabled) return;
        suggestReply(conversationId, new LlamaEngine.StreamCallback() {
            @Override
            public void onToken(String token) {
            }

            @Override
            public void onComplete(String full) {
                com.dpouya.aitexty.data.SmsRepository.getInstance(context).sendMessage(conversationId, full.trim());
                if (onSent != null) onSent.run();
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    private AutoReplyRule getRuleForConversation(long conversationId) {
        List<Message> msgs = AppDatabase.getInstance(context).messageDao().getByConversation(conversationId);
        if (msgs.isEmpty()) return null;
        return AppDatabase.getInstance(context).autoReplyRuleDao().getByAddress(msgs.get(msgs.size() - 1).address);
    }
}

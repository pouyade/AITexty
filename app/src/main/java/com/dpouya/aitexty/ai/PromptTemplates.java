package com.dpouya.aitexty.ai;

public class PromptTemplates {
    public static String spamClassification(String sender, String body) {
        return "System: Classify SMS as spam or ham. Reply ONLY: SPAM or HAM.\nUser: " + sender + " " + body;
    }

    public static String autoReply(String history, String persona) {
        return "System: You are an SMS assistant. Persona: " + persona + ". Reply concisely.\nHistory:\n" + history + "\nAssistant:";
    }

    public static String searchRerank(String query, String snippets) {
        return "System: Rank these SMS snippets by relevance to query: \"" + query + "\". Return indices only.\n" + snippets;
    }
}

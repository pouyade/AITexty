package com.dpouya.aitexty.ai;

import android.content.Context;

import com.dpouya.aitexty.helper.AppSettings;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LlamaEngine {
    private static LlamaEngine instance;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean nativeLoaded;
    private boolean modelReady;

    public interface StreamCallback {
        void onToken(String token);
        void onComplete(String full);
        void onError(String error);
    }

    private LlamaEngine(Context context) {
        this.context = context.getApplicationContext();
        tryLoadNative();
    }

    public static synchronized LlamaEngine getInstance(Context context) {
        if (instance == null) {
            instance = new LlamaEngine(context);
        }
        return instance;
    }

    private void tryLoadNative() {
        try {
            System.loadLibrary("jllama");
            nativeLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            nativeLoaded = false;
        }
        String path = AppSettings.String(AppSettings.Key.AI_MODEL_PATH);
        modelReady = nativeLoaded && path != null && !path.isEmpty() && new File(path).exists();
    }

    public boolean isReady() {
        return modelReady;
    }

    public void generate(String prompt, StreamCallback callback) {
        executor.execute(() -> {
            if (modelReady) {
                try {
                    String result = generateNative(prompt);
                    if (callback != null) {
                        ApplicationLoaderPost(callback, result);
                    }
                } catch (Exception e) {
                    String fallback = generateHeuristic(prompt);
                    ApplicationLoaderPost(callback, fallback);
                }
            } else {
                String fallback = generateHeuristic(prompt);
                ApplicationLoaderPost(callback, fallback);
            }
        });
    }

    private void ApplicationLoaderPost(StreamCallback callback, String result) {
        com.dpouya.aitexty.ApplicationLoader.runOnUiThread(() -> {
            if (callback != null) {
                callback.onComplete(result);
            }
        });
    }

    private native String generateNative(String prompt);

    private String generateHeuristic(String prompt) {
        if (prompt.contains("SPAM or HAM")) {
            return classifyHeuristic(prompt);
        }
        if (prompt.contains("Reply concisely")) {
            return "Thanks for your message. I'll get back to you soon.";
        }
        if (prompt.contains("Rank these")) {
            return "0,1,2";
        }
        return "OK";
    }

    private String classifyHeuristic(String prompt) {
        String lower = prompt.toLowerCase();
        String[] spamWords = {"win", "free", "click", "offer", "lottery", "prize", "urgent", "bitcoin", "loan", "casino"};
        int score = 0;
        for (String w : spamWords) {
            if (lower.contains(w)) score++;
        }
        if (score >= 2) return "SPAM";
        if (score == 1) return "UNCERTAIN";
        return "HAM";
    }

    public String rerankSearch(String query, java.util.List<String> snippets) {
        String lowerQuery = query.toLowerCase();
        java.util.List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < snippets.size(); i++) {
            if (snippets.get(i).toLowerCase().contains(lowerQuery)) {
                indices.add(i);
            }
        }
        for (int i = 0; i < snippets.size(); i++) {
            if (!indices.contains(i)) indices.add(i);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(indices.get(i));
        }
        return sb.toString();
    }
}

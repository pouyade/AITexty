package com.dpouya.aitexty.ai;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class InferenceWorker extends Worker {
    public InferenceWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String prompt = getInputData().getString("prompt");
        if (prompt == null) return Result.failure();
        final boolean[] done = {false};
        final String[] result = {null};
        LlamaEngine.getInstance(getApplicationContext()).generate(prompt, new LlamaEngine.StreamCallback() {
            @Override
            public void onToken(String token) {
            }

            @Override
            public void onComplete(String full) {
                result[0] = full;
                done[0] = true;
            }

            @Override
            public void onError(String error) {
                done[0] = true;
            }
        });
        long start = System.currentTimeMillis();
        while (!done[0] && System.currentTimeMillis() - start < 30000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        return result[0] != null ? Result.success() : Result.failure();
    }
}

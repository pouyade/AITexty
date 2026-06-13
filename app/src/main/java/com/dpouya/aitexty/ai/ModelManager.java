package com.dpouya.aitexty.ai;

import android.content.Context;

import com.dpouya.aitexty.helper.AppSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelManager {
    private static ModelManager instance;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface DownloadCallback {
        void onProgress(int percent);
        void onComplete(String path);
        void onError(String error);
    }

    private ModelManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ModelManager getInstance(Context context) {
        if (instance == null) {
            instance = new ModelManager(context);
        }
        return instance;
    }

    public File getModelsDir() {
        File dir = new File(context.getFilesDir(), "models");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public String getActiveModelPath() {
        String path = AppSettings.String(AppSettings.Key.AI_MODEL_PATH);
        return path != null ? path : "";
    }

    public void setActiveModelPath(String path) {
        AppSettings.String(AppSettings.Key.AI_MODEL_PATH, path);
    }

    public void downloadModel(String url, DownloadCallback callback) {
        executor.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.connect();
                int total = conn.getContentLength();
                InputStream in = conn.getInputStream();
                File out = new File(getModelsDir(), "model.gguf");
                FileOutputStream fos = new FileOutputStream(out);
                byte[] buf = new byte[8192];
                int read, downloaded = 0;
                while ((read = in.read(buf)) != -1) {
                    fos.write(buf, 0, read);
                    downloaded += read;
                    if (total > 0 && callback != null) {
                        callback.onProgress((downloaded * 100) / total);
                    }
                }
                fos.close();
                in.close();
                setActiveModelPath(out.getAbsolutePath());
                AppSettings.Bool(AppSettings.Key.LLM_LOADED, true);
                if (callback != null) callback.onComplete(out.getAbsolutePath());
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }
}

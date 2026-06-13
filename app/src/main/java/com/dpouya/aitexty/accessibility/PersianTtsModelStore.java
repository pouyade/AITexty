package com.dpouya.aitexty.accessibility;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.dpouya.aitexty.helper.LocaleController;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PersianTtsModelStore {
    private static final String TAG = "PersianTtsModelStore";
    private static final String ESPEAK_ASSET_DIR = "espeak-ng-data";
    private static final String DOWNLOAD_BASE =
            "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/";

    public interface PrepareCallback {
        void onReady();

        void onError(String message);
    }

    public interface PrepareProgressCallback extends PrepareCallback {
        void onPhase(String messageKey);

        void onProgress(int percent);
    }

    private static final byte[] ONNX_MAGIC = {
            0x08, 0x09, 0x12, 0x07, 0x70, 0x79, 0x74, 0x6f, 0x72, 0x63, 0x68
    };

    private static WeakReference<Activity> progressActivityRef;
    private static final Map<PersianTtsVoice, List<PrepareCallback>> activePrepareCallbacks = new HashMap<>();

    private PersianTtsModelStore() {
    }

    public static void setProgressActivity(Activity activity) {
        progressActivityRef = activity != null ? new WeakReference<>(activity) : null;
    }

    public static Activity getProgressActivity() {
        return progressActivityRef != null ? progressActivityRef.get() : null;
    }

    public static boolean isVoiceReady(Context context, PersianTtsVoice voice) {
        return validateVoiceModel(context, voice);
    }

    public static boolean validateVoiceModelStrict(Context context, PersianTtsVoice voice) {
        if (!validateVoiceModel(context, voice)) {
            return false;
        }
        File onnx = new File(getModelDir(context, voice), voice.modelFileName);
        if (voice.expectedOnnxBytes > 0 && onnx.length() != voice.expectedOnnxBytes) {
            Log.w(TAG, voice.id + " model failed exact size check: " + onnx.length());
            return false;
        }
        if (voice.onnxSha256 != null && !voice.onnxSha256.equalsIgnoreCase(sha256Hex(onnx))) {
            Log.w(TAG, voice.id + " model failed SHA-256 check");
            return false;
        }
        return true;
    }

    public static boolean needsDownload(PersianTtsVoice voice) {
        return voice.downloadArchive != null;
    }

    public static File getModelDir(Context context, PersianTtsVoice voice) {
        File root = getStorageRoot(context);
        return new File(root, voice.modelDirName);
    }

    public static File getStorageRoot(Context context) {
        File root = context.getExternalFilesDir(null);
        if (root == null) {
            root = context.getFilesDir();
        }
        return root;
    }

    private static File getEspeakDir(Context context) {
        return new File(getStorageRoot(context), ESPEAK_ASSET_DIR);
    }

    public static boolean validateVoiceModel(Context context, PersianTtsVoice voice) {
        File modelDir = getModelDir(context, voice);
        File onnx = new File(modelDir, voice.modelFileName);
        File tokens = new File(modelDir, "tokens.txt");
        if (!onnx.exists() || !tokens.exists()) {
            return false;
        }
        if (onnx.length() < voice.minOnnxBytes || tokens.length() < 32) {
            Log.w(TAG, voice.id + " model failed size check: onnx=" + onnx.length()
                    + " tokens=" + tokens.length());
            return false;
        }
        if (voice.expectedOnnxBytes > 0 && onnx.length() != voice.expectedOnnxBytes) {
            Log.w(TAG, voice.id + " model failed expected size check: onnx=" + onnx.length());
            return false;
        }
        File espeakDir = resolveEspeakDir(context, voice);
        if (!espeakDir.exists() || espeakDir.list() == null || espeakDir.list().length == 0) {
            return false;
        }
        if (!isValidOnnxFile(onnx)) {
            Log.w(TAG, voice.id + " model failed ONNX header check");
            return false;
        }
        return true;
    }

    public static File resolveEspeakDir(Context context, PersianTtsVoice voice) {
        File modelEspeak = new File(getModelDir(context, voice), ESPEAK_ASSET_DIR);
        if (modelEspeak.exists() && modelEspeak.list() != null && modelEspeak.list().length > 0) {
            return modelEspeak;
        }
        return getEspeakDir(context);
    }

    public static void deleteVoiceModel(Context context, PersianTtsVoice voice) {
        deleteRecursively(getModelDir(context, voice));
        if (voice.downloadArchive != null) {
            File archive = new File(getStorageRoot(context), voice.downloadArchive);
            if (archive.exists()) {
                archive.delete();
            }
        }
    }

    public static void prepareVoice(Context context, PersianTtsVoice voice, PrepareCallback callback) {
        if (validateVoiceModelStrict(context, voice)) {
            callback.onReady();
            return;
        }

        synchronized (PersianTtsModelStore.class) {
            List<PrepareCallback> waiters = activePrepareCallbacks.get(voice);
            if (waiters != null) {
                waiters.add(callback);
                return;
            }
            waiters = new ArrayList<>();
            waiters.add(callback);
            activePrepareCallbacks.put(voice, waiters);
        }

        deleteVoiceModel(context, voice);

        boolean willDownload = voice.downloadArchive != null;
        Activity activity = progressActivityRef != null ? progressActivityRef.get() : null;
        if (willDownload && activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(() -> PersianTtsDownloadDialog.show(activity, voice));
        }

        PrepareProgressCallback progressCallback = callback instanceof PrepareProgressCallback
                ? (PrepareProgressCallback) callback
                : null;

        new Thread(() -> {
            String errorMessage = null;
            try {
                notifyPhase(progressCallback, "persian_voice_preparing");
                ensureEspeakData(context);
                if (voice.downloadArchive == null) {
                    copyAssetDir(context, voice.modelDirName, voice);
                } else {
                    downloadAndExtract(context, voice, progressCallback);
                }
                if (!validateVoiceModelStrict(context, voice)) {
                    deleteVoiceModel(context, voice);
                    errorMessage = LocaleController.getString("persian_voice_download_failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "prepareVoice failed for " + voice.id, e);
                deleteVoiceModel(context, voice);
                errorMessage = e.getMessage() != null ? e.getMessage()
                        : LocaleController.getString("persian_voice_download_failed");
            }
            dismissDialog();
            finishPrepare(voice, errorMessage);
        }, "PersianTtsModel-" + voice.id).start();
    }

    private static void finishPrepare(PersianTtsVoice voice, String errorMessage) {
        List<PrepareCallback> callbacks;
        synchronized (PersianTtsModelStore.class) {
            callbacks = activePrepareCallbacks.remove(voice);
        }
        if (callbacks == null) {
            return;
        }
        for (PrepareCallback callback : callbacks) {
            if (errorMessage == null) {
                callback.onReady();
            } else {
                callback.onError(errorMessage);
            }
        }
    }

    private static void dismissDialog() {
        PersianTtsDownloadDialog.dismissActive();
    }

    private static void notifyPhase(PrepareProgressCallback callback, String messageKey) {
        if (callback != null) {
            callback.onPhase(messageKey);
        }
        PersianTtsDownloadDialog.update(-1, messageKey);
    }

    private static void notifyProgress(PrepareProgressCallback callback, int percent) {
        if (callback != null) {
            callback.onProgress(percent);
        }
        PersianTtsDownloadDialog.update(percent, "persian_voice_downloading");
    }

    private static String sha256Hex(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1024];
            try (InputStream in = new FileInputStream(file)) {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format(Locale.US, "%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.w(TAG, "sha256Hex failed for " + file, e);
            return "";
        }
    }

    private static boolean isValidOnnxFile(File file) {
        try (InputStream in = new FileInputStream(file)) {
            byte[] header = new byte[ONNX_MAGIC.length];
            if (in.read(header) != header.length) {
                return false;
            }
            for (int i = 0; i < header.length; i++) {
                if (header[i] != ONNX_MAGIC[i]) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isValidArchive(File archive, PersianTtsVoice voice) {
        if (!archive.exists() || archive.length() < voice.minArchiveBytes) {
            return false;
        }
        try (InputStream in = new FileInputStream(archive)) {
            byte[] header = new byte[3];
            if (in.read(header) != 3) {
                return false;
            }
            return header[0] == 'B' && header[1] == 'Z' && header[2] == 'h';
        } catch (IOException e) {
            return false;
        }
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    private static void ensureEspeakData(Context context) throws IOException {
        File target = getEspeakDir(context);
        if (target.exists() && target.list() != null && target.list().length > 0) {
            return;
        }
        copyAssetDir(context, ESPEAK_ASSET_DIR);
    }

    private static void copyAssetDir(Context context, String assetPath) throws IOException {
        copyAssetDir(context, assetPath, null);
    }

    private static void copyAssetDir(Context context, String assetPath, PersianTtsVoice voice)
            throws IOException {
        File root = getStorageRoot(context);
        copyAssetsRecursive(context, assetPath, new File(root, assetPath), voice);
    }

    private static void copyAssetsRecursive(Context context, String assetPath, File destDir,
                                            PersianTtsVoice voice) throws IOException {
        String[] children = context.getAssets().list(assetPath);
        if (children == null || children.length == 0) {
            destDir.getParentFile().mkdirs();
            copyAssetFile(context, assetPath, destDir, voice);
            return;
        }
        destDir.mkdirs();
        for (String child : children) {
            String childPath = assetPath.isEmpty() ? child : assetPath + "/" + child;
            copyAssetsRecursive(context, childPath, new File(destDir, child), voice);
        }
    }

    private static void copyAssetFile(Context context, String assetPath, File destFile,
                                      PersianTtsVoice voice) throws IOException {
        if (destFile.exists()) {
            if (assetPath.endsWith(".onnx") && voice != null && voice.onnxSha256 != null) {
                if (destFile.length() == voice.expectedOnnxBytes
                        && isValidOnnxFile(destFile)
                        && voice.onnxSha256.equalsIgnoreCase(sha256Hex(destFile))) {
                    return;
                }
            } else if (destFile.length() > 0) {
                return;
            }
            destFile.delete();
        }
        destFile.getParentFile().mkdirs();
        try (InputStream in = context.getAssets().open(assetPath);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private static void downloadAndExtract(Context context, PersianTtsVoice voice,
                                             PrepareProgressCallback callback) throws IOException {
        File root = getStorageRoot(context);
        root.mkdirs();
        File archiveFile = new File(root, voice.downloadArchive);
        if (!isValidArchive(archiveFile, voice)) {
            if (archiveFile.exists()) {
                archiveFile.delete();
            }
            deleteRecursively(getModelDir(context, voice));
            notifyPhase(callback, "persian_voice_downloading");
            downloadFile(DOWNLOAD_BASE + voice.downloadArchive, archiveFile, voice, callback);
        }
        if (!isValidArchive(archiveFile, voice)) {
            throw new IOException("Downloaded archive is invalid for " + voice.id);
        }
        notifyPhase(callback, "persian_voice_extracting");
        extractTarBz2(archiveFile, root);
    }

    private static void downloadFile(String urlString, File dest, PersianTtsVoice voice,
                                     PrepareProgressCallback callback) throws IOException {
        Log.i(TAG, "Downloading " + urlString);
        HttpURLConnection connection = null;
        File tempFile = new File(dest.getAbsolutePath() + ".part");
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(180000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + " for " + urlString);
            }
            long length = connection.getContentLengthLong();
            if (tempFile.exists()) {
                tempFile.delete();
            }
            tempFile.getParentFile().mkdirs();
            long total = 0;
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    total += read;
                    if (length > 0) {
                        int percent = (int) Math.min(100, (total * 100L) / length);
                        notifyProgress(callback, percent);
                    }
                }
                out.getFD().sync();
            }
            if (length > 0 && total != length) {
                throw new IOException("Incomplete download: " + total + "/" + length);
            }
            if (tempFile.length() < voice.minArchiveBytes) {
                throw new IOException("Download too small: " + tempFile.length());
            }
            if (!isValidArchive(tempFile, voice)) {
                throw new IOException("Downloaded file is not a valid archive");
            }
            if (dest.exists() && !dest.delete()) {
                throw new IOException("Could not replace existing archive");
            }
            if (!tempFile.renameTo(dest)) {
                throw new IOException("Could not finalize downloaded archive");
            }
            notifyProgress(callback, 100);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static void extractTarBz2(File archiveFile, File destDir) throws IOException {
        try (InputStream fi = new FileInputStream(archiveFile);
             InputStream bi = new BufferedInputStream(fi);
             BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bi);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn)) {
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                String name = entry.getName();
                if (name.contains("..")) {
                    continue;
                }
                File outFile = new File(destDir, name);
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                outFile.getParentFile().mkdirs();
                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[64 * 1024];
                    int read;
                    while ((read = tarIn.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        }
    }
}

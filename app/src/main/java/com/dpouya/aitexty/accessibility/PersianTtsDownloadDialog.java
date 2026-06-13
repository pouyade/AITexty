package com.dpouya.aitexty.accessibility;

import android.app.Activity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Build;

import com.dpouya.aitexty.helper.LocaleController;

import java.lang.ref.WeakReference;

public final class PersianTtsDownloadDialog {
    private static PersianTtsDownloadDialog active;

    private ProgressDialog dialog;
    private WeakReference<Activity> activityRef;

    public static void show(Activity activity, PersianTtsVoice voice) {
        dismissActive();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        active = new PersianTtsDownloadDialog();
        active.activityRef = new WeakReference<>(activity);
        active.dialog = new ProgressDialog(activity);
        active.dialog.setTitle(voice.getLabel());
        active.dialog.setMessage(LocaleController.getString("persian_voice_downloading"));
        active.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        active.dialog.setMax(100);
        active.dialog.setProgress(0);
        active.dialog.setCancelable(false);
        active.dialog.show();
    }

    public static void update(int percent, String messageKey) {
        if (active == null || active.dialog == null || !active.dialog.isShowing()) {
            return;
        }
        Activity activity = active.activityRef != null ? active.activityRef.get() : null;
        if (activity == null || activity.isFinishing()) {
            dismissActive();
            return;
        }
        activity.runOnUiThread(() -> {
            if (active == null || active.dialog == null || !active.dialog.isShowing()) {
                return;
            }
            if (percent >= 0) {
                active.dialog.setIndeterminate(false);
                active.dialog.setProgress(Math.min(100, Math.max(0, percent)));
            } else {
                active.dialog.setIndeterminate(true);
            }
            if (messageKey != null && !messageKey.isEmpty()) {
                active.dialog.setMessage(LocaleController.getString(messageKey));
            }
        });
    }

    public static void dismissActive() {
        if (active == null) {
            return;
        }
        Activity activity = active.activityRef != null ? active.activityRef.get() : null;
        Runnable dismiss = () -> {
            if (active != null && active.dialog != null && active.dialog.isShowing()) {
                try {
                    active.dialog.dismiss();
                } catch (Exception ignored) {
                }
            }
            active = null;
        };
        if (activity != null && !activity.isFinishing()) {
            activity.runOnUiThread(dismiss);
        } else {
            dismiss.run();
        }
    }

    public static void showError(Activity activity, String message) {
        dismissActive();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(LocaleController.getString("persian_voice_status"))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}

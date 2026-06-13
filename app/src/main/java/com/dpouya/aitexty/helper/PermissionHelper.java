package com.dpouya.aitexty.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class PermissionHelper {
    public static final int REQUEST_RECORD_AUDIO = 9101;
    public static final int REQUEST_READ_CONTACTS = 9102;

    private PermissionHelper() {
    }

    public static boolean hasRecordAudio(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestRecordAudio(Activity activity) {
        if (hasRecordAudio(activity)) {
            return true;
        }
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO);
        return false;
    }

    public static boolean hasReadContacts(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestReadContacts(Activity activity) {
        if (hasReadContacts(activity)) {
            return true;
        }
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_READ_CONTACTS);
        return false;
    }
}

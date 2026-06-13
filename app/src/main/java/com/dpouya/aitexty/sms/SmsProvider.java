package com.dpouya.aitexty.sms;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.Telephony;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SmsProvider extends ContentProvider {
    private static final int SMS_ALL = 1;
    private static final int SMS_ID = 2;
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        matcher.addURI("sms", "#", SMS_ALL);
        matcher.addURI("sms", "#/#", SMS_ID);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return getContext().getContentResolver().query(
                Telephony.Sms.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android-dir/sms";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) return null;
        return getContext().getContentResolver().insert(Telephony.Sms.CONTENT_URI, values);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return getContext().getContentResolver().delete(Telephony.Sms.CONTENT_URI, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return getContext().getContentResolver().update(Telephony.Sms.CONTENT_URI, values, selection, selectionArgs);
    }
}

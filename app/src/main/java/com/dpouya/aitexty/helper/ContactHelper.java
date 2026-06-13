package com.dpouya.aitexty.helper;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.dpouya.aitexty.data.ContactEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ContactHelper {
    private static final String TAG = "ContactHelper";

    public static String resolveDisplayName(Context context, String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }
        if (!hasReadContactsPermission(context)) {
            return address;
        }
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    if (idx >= 0) {
                        String name = cursor.getString(idx);
                        if (!TextUtils.isEmpty(name)) {
                            return name;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "resolveDisplayName failed for " + address, e);
        }
        return address;
    }

    public static Uri resolvePhotoUri(Context context, String address) {
        if (address == null || address.isEmpty() || !hasReadContactsPermission(context)) {
            return null;
        }
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
            String[] projection = new String[]{ContactsContract.PhoneLookup.PHOTO_URI};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI);
                    if (idx >= 0) {
                        String photo = cursor.getString(idx);
                        if (!TextUtils.isEmpty(photo)) {
                            return Uri.parse(photo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "resolvePhotoUri failed for " + address, e);
        }
        return null;
    }

    public static String getInitial(String name) {
        if (TextUtils.isEmpty(name)) {
            return "?";
        }
        String trimmed = name.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c)) {
                return String.valueOf(Character.toUpperCase(c));
            }
        }
        return "?";
    }

    public static int getAvatarColor(String seed) {
        if (seed == null) seed = "";
        int hash = seed.hashCode();
        float hue = (hash & 0xFFFF) % 360f;
        return Color.HSVToColor(new float[]{hue, 0.45f, 0.75f});
    }

    public static String normalizePhone(String number) {
        if (number == null) {
            return "";
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (c == '+' && digits.length() == 0) {
                digits.append(c);
            }
        }
        return digits.toString();
    }

    public static boolean hasReadContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static List<ContactEntry> loadContacts(Context context) {
        List<ContactEntry> contacts = new ArrayList<>();
        if (!hasReadContactsPermission(context)) {
            return contacts;
        }

        Set<String> seen = new HashSet<>();
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        };
        try (Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {
            if (cursor == null) {
                Log.w(TAG, "Phone contacts query returned null cursor");
                return contacts;
            }
            int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int normalizedIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
            int idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            while (cursor.moveToNext()) {
                String number = numberIdx >= 0 ? cursor.getString(numberIdx) : "";
                if (TextUtils.isEmpty(number)) {
                    continue;
                }
                String normalized = normalizedIdx >= 0 ? cursor.getString(normalizedIdx) : null;
                if (TextUtils.isEmpty(normalized)) {
                    normalized = normalizePhone(number);
                }
                if (TextUtils.isEmpty(normalized) || seen.contains(normalized)) {
                    continue;
                }
                seen.add(normalized);

                long contactId = idIdx >= 0 ? cursor.getLong(idIdx) : 0;
                String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "";
                if (TextUtils.isEmpty(name)) {
                    name = resolveContactName(context, contactId, number);
                }
                contacts.add(new ContactEntry(name.trim(), number.trim(), contactId));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load contacts", e);
        }
        return contacts;
    }

    private static String resolveContactName(Context context, long contactId, String fallback) {
        if (contactId <= 0) {
            return fallback;
        }
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY},
                null,
                null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (!TextUtils.isEmpty(name)) {
                    return name;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "resolveContactName failed for id " + contactId, e);
        }
        return fallback;
    }

    public static List<ContactEntry> filterContacts(List<ContactEntry> source, String query) {
        if (source == null) {
            return new ArrayList<>();
        }
        if (TextUtils.isEmpty(query)) {
            return new ArrayList<>(source);
        }
        String q = query.trim().toLowerCase(Locale.getDefault());
        String qDigits = normalizePhone(q);
        List<ContactEntry> filtered = new ArrayList<>();
        for (ContactEntry entry : source) {
            if (entry.displayName != null && entry.displayName.toLowerCase(Locale.getDefault()).contains(q)) {
                filtered.add(entry);
                continue;
            }
            if (entry.phoneNumber != null) {
                String phoneDigits = normalizePhone(entry.phoneNumber);
                if (entry.phoneNumber.contains(q)
                        || (!qDigits.isEmpty() && phoneDigits.contains(qDigits))) {
                    filtered.add(entry);
                }
            }
        }
        return filtered;
    }
}

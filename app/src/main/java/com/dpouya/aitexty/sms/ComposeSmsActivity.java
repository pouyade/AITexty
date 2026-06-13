package com.dpouya.aitexty.sms;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.dpouya.aitexty.activities.ChatActivity;

public class ComposeSmsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            String address = data.getSchemeSpecificPart();
            if (!TextUtils.isEmpty(address)) {
                Intent chat = new Intent(this, ChatActivity.class);
                chat.putExtra("address", address);
                startActivity(chat);
            }
        }
        finish();
    }
}

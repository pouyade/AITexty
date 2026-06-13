package com.dpouya.aitexty.activities;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;

public class OnboardingActivity extends BaseActivity {
    private static final int PERM_REQUEST = 100;
    private static final int ROLE_REQUEST = 101;

    @Override
    protected boolean showActionbar() {
        return false;
    }

    @Override
    protected android.view.View initViews(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        TextView title = new TextView(context);
        title.setText(LocaleController.getString("onboarding_title"));
        title.setTextSize(22);
        title.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        title.setGravity(Gravity.CENTER);
        layout.addView(title, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));

        TextView sub = new TextView(context);
        sub.setText(LocaleController.getString("onboarding_subtitle"));
        sub.setTextSize(14);
        sub.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        sub.setGravity(Gravity.CENTER);
        layout.addView(sub, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 40));

        Button permBtn = new Button(context);
        permBtn.setText(LocaleController.getString("grant_permissions"));
        permBtn.setOnClickListener(v -> requestPermissions());
        layout.addView(permBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        Button smsBtn = new Button(context);
        smsBtn.setText(LocaleController.getString("set_default_sms"));
        smsBtn.setOnClickListener(v -> requestDefaultSms());
        layout.addView(smsBtn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return layout;
    }

    private void requestPermissions() {
        String[] perms = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms = new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        }
        ActivityCompat.requestPermissions(this, perms, PERM_REQUEST);
    }

    private void requestDefaultSms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager rm = getSystemService(RoleManager.class);
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_SMS)) {
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_SMS), ROLE_REQUEST);
                return;
            }
        }
        Intent intent = new Intent(android.provider.Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(android.provider.Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        startActivityForResult(intent, ROLE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ROLE_REQUEST && resultCode == RESULT_OK) {
            finishOnboarding();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST) {
            requestDefaultSms();
        }
    }

    private void finishOnboarding() {
        AppSettings.Bool(AppSettings.Key.IS_INTRO_COMPLETED, true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

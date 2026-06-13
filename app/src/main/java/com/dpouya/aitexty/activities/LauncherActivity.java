package com.dpouya.aitexty.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dpouya.aitexty.helper.AppSettings;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppSettings.Bool(AppSettings.Key.IS_INTRO_COMPLETED)) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, OnboardingActivity.class));
        }
        finish();
    }
}

package com.dpouya.aitexty.activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.SpacesItemDecoration;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.cells.SettingsCell;
import com.dpouya.aitexty.ui.adapter.SettingsAdapter;

public class SettingsActivity extends BaseActivity {
    @Override
    protected View initViews(Context context) {
        RecyclerView list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.addItemDecoration(new SpacesItemDecoration(AndroidUtilities.dp5, AndroidUtilities.dp5, AndroidUtilities.dp5, 0));
        list.setBackgroundColor(Theme.getColor(Theme.BACKGROUND_COLOR));

        SettingsAdapter adapter = new SettingsAdapter(context, key -> handleSetting(key));
        list.setAdapter(adapter);
        return list;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("settings"));
    }

    private void handleSetting(String key) {
        switch (key) {
            case "theme":
                Theme.toggleNightMode();
                break;
            case "language_fa":
                LocaleController.changeLanguage("fa");
                break;
            case "language_en":
                LocaleController.changeLanguage("en");
                break;
            case "blocklist":
                startActivity(new Intent(this, BlocklistActivity.class));
                break;
            case "privacy":
                AppSettings.String(AppSettings.Key.HIDDEN_UNLOCK_METHOD,
                        "gesture".equals(AppSettings.String(AppSettings.Key.HIDDEN_UNLOCK_METHOD)) ? "pin" : "gesture");
                break;
            case "ai":
                startActivity(new Intent(this, AiSettingsActivity.class));
                break;
            case "accessibility":
                startActivity(new Intent(this, AccessibilitySettingsActivity.class));
                break;
            default:
                break;
        }
    }
}

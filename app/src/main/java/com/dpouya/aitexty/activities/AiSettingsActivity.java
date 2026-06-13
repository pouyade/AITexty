package com.dpouya.aitexty.activities;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dpouya.aitexty.ai.ModelManager;
import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.AppSettings;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;

public class AiSettingsActivity extends BaseActivity {
    @Override
    protected View initViews(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20);

        Button autoReply = new Button(context);
        autoReply.setText(LocaleController.getString("auto_reply"));
        autoReply.setOnClickListener(v ->
                AppSettings.Bool(AppSettings.Key.AUTO_REPLY_ENABLED,
                        !AppSettings.Bool(AppSettings.Key.AUTO_REPLY_ENABLED)));
        layout.addView(autoReply, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        Button spamBlock = new Button(context);
        spamBlock.setText(LocaleController.getString("spam_detected") + " auto-block");
        spamBlock.setOnClickListener(v ->
                AppSettings.Bool(AppSettings.Key.SPAM_AUTO_BLOCK,
                        !AppSettings.Bool(AppSettings.Key.SPAM_AUTO_BLOCK)));
        layout.addView(spamBlock, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));

        Button modelInfo = new Button(context);
        modelInfo.setText(LocaleController.getString("download_model"));
        modelInfo.setOnClickListener(v -> {
            String path = ModelManager.getInstance(context).getActiveModelPath();
            Toast.makeText(context, path.isEmpty() ? "Place GGUF in files/models/model.gguf" : path, Toast.LENGTH_LONG).show();
        });
        layout.addView(modelInfo, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return layout;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("ai_settings"));
    }
}

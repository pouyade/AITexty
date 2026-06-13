package com.dpouya.aitexty.activities;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.ChatThemeManager;

public class ChatThemePickerActivity extends BaseActivity {
    private long conversationId;

    @Override
    protected View initViews(Context context) {
        conversationId = getIntent().getLongExtra("conversation_id", -1);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20, AndroidUtilities.dp20);

        String[] presets = {"#E8E8E8", "#DCF8C6", "#383543", "#2E4A3E"};
        for (String incoming : presets) {
            Button btn = new Button(context);
            btn.setText(incoming);
            btn.setOnClickListener(v -> {
                ChatThemeManager.saveTheme(context, conversationId, incoming, "#DCF8C6", "#EEEEEE");
                Toast.makeText(context, LocaleController.getString("chat_theme"), Toast.LENGTH_SHORT).show();
                finish();
            });
            layout.addView(btn, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8));
        }
        return layout;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("chat_theme"));
    }
}

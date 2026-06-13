package com.dpouya.aitexty.ui.cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;

public class SettingsCell extends FrameLayout {
    public interface Listener {
        void onClick(String key);
    }

    private final TextView titleView;
    private String key;
    private Listener listener;

    public SettingsCell(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setPadding(AndroidUtilities.dp15, AndroidUtilities.dp12, AndroidUtilities.dp15, AndroidUtilities.dp12);
        titleView = new TextView(context);
        titleView.setTextSize(16);
        addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT)));
        setOnClickListener(v -> {
            if (key != null && this.listener != null) this.listener.onClick(key);
        });
    }

    public void bind(String key, String title) {
        this.key = key;
        titleView.setText(title);
        titleView.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        setBackgroundColor(Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
    }
}

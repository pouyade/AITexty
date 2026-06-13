package com.dpouya.aitexty.ui.cells;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dpouya.aitexty.data.entity.BlockEntry;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;

public class BlocklistCell extends FrameLayout {
    public interface Listener {
        void onUnblock(BlockEntry entry);
    }

    private final TextView nameView;
    private final TextView modeView;
    private BlockEntry entry;
    private Listener listener;

    public BlocklistCell(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setPadding(AndroidUtilities.dp10, AndroidUtilities.dp10, AndroidUtilities.dp10, AndroidUtilities.dp10);

        nameView = new TextView(context);
        nameView.setTextSize(16);
        addView(nameView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT)));

        modeView = new TextView(context);
        modeView.setTextSize(12);
        addView(modeView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT)));

        setOnClickListener(v -> {
            if (entry != null && this.listener != null) this.listener.onUnblock(entry);
        });
    }

    public void setEntry(BlockEntry e) {
        this.entry = e;
        nameView.setText(e.displayName != null ? e.displayName : e.phoneNumber);
        modeView.setText(e.blockMode);
        nameView.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        modeView.setTextColor(Theme.getColor(Theme.BLOCK_BADGE_COLOR));
        setBackgroundColor(Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
    }
}

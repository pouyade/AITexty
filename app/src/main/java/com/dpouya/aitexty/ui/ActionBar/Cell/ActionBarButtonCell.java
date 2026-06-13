package com.dpouya.aitexty.ui.ActionBar.Cell;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dpouya.aitexty.R;
import com.dpouya.aitexty.components.FaIconView;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.ui.ActionBar.ActionBarButton;

public class ActionBarButtonCell extends LinearLayout implements View.OnClickListener {
    private ActionBarButton pactionbarButton;
    private FaIconView iconView;
    private TextView txtTitle;
    private Typeface typeface;

    public ActionBarButtonCell(Context context, Typeface typeface) {
        super(context);
        this.typeface = typeface;
        init();
    }

    private void init() {
        setLayoutParams(LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));
        setOrientation(HORIZONTAL);
        setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.actionbar_button_background));
        txtTitle = new TextView(getContext());
        txtTitle.setTextColor(0xffffffff);
        txtTitle.setTypeface(this.typeface);
        txtTitle.setVisibility(GONE);
        txtTitle.setLines(1);
        txtTitle.setMaxLines(1);
        txtTitle.setGravity(Gravity.CENTER);
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        addView(txtTitle, LayoutHelper.createLinear(50, LayoutHelper.MATCH_PARENT));
        iconView = new FaIconView(getContext(), 18);
        iconView.setVisibility(GONE);
        addView(iconView, LayoutHelper.createLinear(30, 30, Gravity.CENTER_VERTICAL));
        setOnClickListener(this);
    }

    public void setActionBarButton(ActionBarButton pactionbarButton) {
        this.pactionbarButton = pactionbarButton;
        if (pactionbarButton.title == null) {
            txtTitle.setVisibility(GONE);
        } else {
            txtTitle.setText(pactionbarButton.title);
            txtTitle.setVisibility(VISIBLE);
        }

        if (pactionbarButton.faIcon != null) {
            iconView.setIcon(pactionbarButton.faIcon);
            iconView.setIconColor(pactionbarButton.iconColor);
            iconView.setVisibility(VISIBLE);
        } else if (pactionbarButton.drawable != null) {
            iconView.setVisibility(GONE);
        } else {
            iconView.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View view) {
        pactionbarButton.onClickListener.onClick(view);
    }
}

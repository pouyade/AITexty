package com.dpouya.aitexty.ui.ActionBar;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.dpouya.aitexty.helper.FontAwesome;

public class ActionBarButton {
    public String title;
    public Drawable drawable;
    public FontAwesome.Icon faIcon;
    public int iconColor;
    public View.OnClickListener onClickListener;

    public ActionBarButton(String title, FontAwesome.Icon faIcon, int iconColor, View.OnClickListener onClickListener) {
        this.title = title;
        this.faIcon = faIcon;
        this.iconColor = iconColor;
        this.onClickListener = onClickListener;
    }

    public ActionBarButton(FontAwesome.Icon faIcon, int iconColor, View.OnClickListener onClickListener) {
        this(null, faIcon, iconColor, onClickListener);
    }

    public ActionBarButton(String title, Drawable drawable, View.OnClickListener onClickListener) {
        this.title = title;
        this.drawable = drawable;
        this.onClickListener = onClickListener;
    }
}

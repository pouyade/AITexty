package com.dpouya.aitexty.components;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.dpouya.aitexty.helper.FontAwesome;

public class FaIconView extends AppCompatTextView {
    private FontAwesome.Icon icon;
    private FontAwesome.Style style = FontAwesome.Style.SOLID;

    public FaIconView(Context context) {
        super(context);
        init(18);
    }

    public FaIconView(Context context, float sizeSp) {
        super(context);
        init(sizeSp);
    }

    private void init(float sizeSp) {
        setGravity(Gravity.CENTER);
        setIncludeFontPadding(false);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        setTypeface(FontAwesome.getTypeface(FontAwesome.Style.SOLID));
    }

    public void setIcon(FontAwesome.Icon icon) {
        setIcon(icon, FontAwesome.Style.SOLID);
    }

    public void setIcon(FontAwesome.Icon icon, FontAwesome.Style style) {
        this.icon = icon;
        this.style = style;
        if (icon != null) {
            setTypeface(FontAwesome.getTypeface(style));
            setText(icon.unicode());
        } else {
            setText("");
        }
    }

    public void setIconColor(int color) {
        setTextColor(color);
    }

    public FontAwesome.Icon getIcon() {
        return icon;
    }
}

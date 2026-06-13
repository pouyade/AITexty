package com.dpouya.aitexty.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.ContactHelper;
import com.dpouya.aitexty.helper.LayoutHelper;

public class ContactAvatarView extends FrameLayout {
    public static final int SIZE_LIST = 52;
    public static final int SIZE_ACTIONBAR = 36;

    private final int sizeDp;
    private final int sizePx;
    private final ImageView photoView;
    private final TextView initialView;

    public ContactAvatarView(Context context) {
        this(context, SIZE_LIST);
    }

    public ContactAvatarView(Context context, int sizeDp) {
        super(context);
        this.sizeDp = sizeDp;
        this.sizePx = AndroidUtilities.dp(sizeDp);

        setMinimumWidth(sizePx);
        setMinimumHeight(sizePx);

        photoView = new ImageView(context);
        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        applyCircularClip(photoView);
        addView(photoView, LayoutHelper.createFrame(sizeDp, sizeDp, Gravity.CENTER));

        initialView = new TextView(context);
        initialView.setGravity(Gravity.CENTER);
        initialView.setTextSize(sizeDp >= SIZE_LIST ? 20 : 14);
        initialView.setTextColor(Color.WHITE);
        addView(initialView, LayoutHelper.createFrame(sizeDp, sizeDp, Gravity.CENTER));
    }

    public static ContactAvatarView create(Context context, int sizeDp) {
        return new ContactAvatarView(context, sizeDp);
    }

    private void applyCircularClip(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setClipToOutline(true);
            view.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View v, Outline outline) {
                    outline.setOval(0, 0, v.getWidth() > 0 ? v.getWidth() : sizePx,
                            v.getHeight() > 0 ? v.getHeight() : sizePx);
                }
            });
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            photoView.invalidateOutline();
        }
    }

    public void bind(String address, String displayName) {
        String name = displayName != null && !displayName.isEmpty() ? displayName : address;
        String initial = ContactHelper.getInitial(name);
        int bgColor = ContactHelper.getAvatarColor(address != null ? address : name);

        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(bgColor);
        initialView.setBackground(circle);
        initialView.setText(initial);
        initialView.setVisibility(VISIBLE);

        Uri photoUri = ContactHelper.resolvePhotoUri(getContext(), address);
        if (photoUri != null) {
            photoView.setImageURI(photoUri);
            photoView.setVisibility(VISIBLE);
            initialView.setVisibility(GONE);
        } else {
            photoView.setImageDrawable(null);
            photoView.setVisibility(GONE);
            initialView.setVisibility(VISIBLE);
        }
        setVisibility(VISIBLE);
    }
}

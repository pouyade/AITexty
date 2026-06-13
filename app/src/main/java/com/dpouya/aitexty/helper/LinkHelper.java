package com.dpouya.aitexty.helper;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.core.text.util.LinkifyCompat;

public final class LinkHelper {
    private static final int LINK_MASK = Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS;

    private LinkHelper() {
    }

    public static void apply(TextView textView, CharSequence text, int linkColor) {
        if (textView == null) {
            return;
        }
        textView.setText(TextUtils.isEmpty(text) ? "" : text);
        LinkifyCompat.addLinks(textView, LINK_MASK);
        textView.setLinkTextColor(linkColor);
        textView.setHighlightColor(AndroidUtilities.adjustAlpha(linkColor, 0.2f));
        textView.setLinksClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

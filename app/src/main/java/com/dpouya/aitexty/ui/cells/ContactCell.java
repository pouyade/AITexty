package com.dpouya.aitexty.ui.cells;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dpouya.aitexty.data.ContactEntry;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.widgets.ContactAvatarView;

public class ContactCell extends FrameLayout {
    public interface Listener {
        void onClick(ContactEntry contact);
    }

    private final ContactAvatarView avatarView;
    private final TextView nameView;
    private final TextView phoneView;
    private ContactEntry contact;
    private Listener listener;

    public ContactCell(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setMinimumHeight(AndroidUtilities.dp(64));
        setPadding(0, AndroidUtilities.dp4, 0, AndroidUtilities.dp4);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp12, AndroidUtilities.dp8, AndroidUtilities.dp12, AndroidUtilities.dp8);
        if (LocaleController.isRTL) {
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        avatarView = ContactAvatarView.create(context, ContactAvatarView.SIZE_LIST);
        row.addView(avatarView, LayoutHelper.createLinear(
                ContactAvatarView.SIZE_LIST, ContactAvatarView.SIZE_LIST, 0, 0, 12, 0));

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        nameView = new TextView(context);
        nameView.setTextSize(16);
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        nameView.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        nameView.setTypeface(Theme.getTypeface(Theme.TypeFaceKey.DEFAULT_TYPEFACE));
        textColumn.addView(nameView, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        phoneView = new TextView(context);
        phoneView.setTextSize(14);
        phoneView.setSingleLine(true);
        phoneView.setEllipsize(TextUtils.TruncateAt.END);
        phoneView.setTextColor(AndroidUtilities.adjustAlpha(Theme.getColor(Theme.TEXT_COLOR), 0.65f));
        textColumn.addView(phoneView, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

        row.addView(textColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));
        addView(row, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        View divider = new View(context);
        divider.setBackgroundColor(AndroidUtilities.adjustAlpha(Theme.getColor(Theme.SUB_TEXT_COLOR), 0.15f));
        addView(divider, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM, AndroidUtilities.dp12, 0, 0, 0));

        setOnClickListener(v -> {
            if (contact != null && listener != null) {
                listener.onClick(contact);
            }
        });
    }

    public void setContact(ContactEntry entry) {
        this.contact = entry;
        nameView.setText(entry.displayName);
        phoneView.setText(entry.phoneNumber);
        avatarView.bind(entry.phoneNumber, entry.displayName);
        setBackgroundColor(Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
    }
}

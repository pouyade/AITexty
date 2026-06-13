package com.dpouya.aitexty.ui.cells;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dpouya.aitexty.components.FaIconView;
import com.dpouya.aitexty.data.AppDatabase;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.ContactHelper;
import com.dpouya.aitexty.helper.FontAwesome;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.ui.widgets.ContactAvatarView;

public class ConversationCell extends FrameLayout {
    public interface Listener {
        void onClick(Conversation conversation);
        void onLongClick(Conversation conversation);
    }

    private static final int CELL_HEIGHT_DP = 72;

    private final FrameLayout avatarSlot;
    private final ContactAvatarView avatarView;
    private final View checkCircle;
    private final FaIconView checkMark;
    private final View unreadDot;
    private final TextView nameView;
    private final TextView previewView;
    private final TextView timeView;
    private final TextView badgeView;
    private Conversation conversation;
    private Listener listener;
    private boolean selectionMode;
    private boolean selected;

    public ConversationCell(Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        setMinimumHeight(AndroidUtilities.dp(CELL_HEIGHT_DP));
        setPadding(0, AndroidUtilities.dp5, 0, AndroidUtilities.dp5);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(AndroidUtilities.dp12, AndroidUtilities.dp8, AndroidUtilities.dp12, AndroidUtilities.dp8);
        if (LocaleController.isRTL) {
            row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        avatarSlot = new FrameLayout(context);
        avatarView = ContactAvatarView.create(context, ContactAvatarView.SIZE_LIST);
        avatarSlot.addView(avatarView, LayoutHelper.createFrame(
                ContactAvatarView.SIZE_LIST, ContactAvatarView.SIZE_LIST, Gravity.CENTER));

        checkCircle = new View(context);
        checkMark = new FaIconView(context, 16);
        checkMark.setIcon(FontAwesome.Icon.CHECK);
        checkMark.setIconColor(Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR));
        checkMark.setVisibility(GONE);
        avatarSlot.addView(checkCircle, LayoutHelper.createFrame(
                ContactAvatarView.SIZE_LIST, ContactAvatarView.SIZE_LIST, Gravity.CENTER));
        avatarSlot.addView(checkMark, LayoutHelper.createFrame(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        checkCircle.setVisibility(GONE);

        row.addView(avatarSlot, LayoutHelper.createLinear(
                ContactAvatarView.SIZE_LIST, ContactAvatarView.SIZE_LIST, 0, 0, 12, 0));

        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        LinearLayout nameRow = new LinearLayout(context);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
        if (LocaleController.isRTL) {
            nameRow.setLayoutDirection(LAYOUT_DIRECTION_RTL);
        }

        unreadDot = new View(context);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        unreadDot.setBackground(dotBg);
        unreadDot.setVisibility(GONE);
        nameRow.addView(unreadDot, LayoutHelper.createLinear(8, 8, Gravity.CENTER_VERTICAL, 0, 0, 8, 0));

        nameView = new TextView(context);
        nameView.setTextSize(16);
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        nameView.setTypeface(Theme.getTypeface(Theme.TypeFaceKey.DEFAULT_TYPEFACE));
        nameRow.addView(nameView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        textColumn.addView(nameRow, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        previewView = new TextView(context);
        previewView.setTextSize(14);
        previewView.setSingleLine(true);
        previewView.setEllipsize(TextUtils.TruncateAt.END);
        previewView.setPadding(0, AndroidUtilities.dp2, 0, 0);
        previewView.setVisibility(VISIBLE);
        textColumn.addView(previewView, LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 0));

        row.addView(textColumn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        LinearLayout metaColumn = new LinearLayout(context);
        metaColumn.setOrientation(LinearLayout.VERTICAL);
        metaColumn.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        metaColumn.setPadding(AndroidUtilities.dp8, 0, 0, 0);

        timeView = new TextView(context);
        timeView.setTextSize(12);
        timeView.setSingleLine(true);
        metaColumn.addView(timeView, LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.END));

        badgeView = new TextView(context);
        badgeView.setTextSize(10);
        badgeView.setGravity(Gravity.CENTER);
        badgeView.setVisibility(GONE);
        badgeView.setMinWidth(AndroidUtilities.dp20);
        badgeView.setMinHeight(AndroidUtilities.dp20);
        metaColumn.addView(badgeView, LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.END, 0, 4, 0, 0));

        row.addView(metaColumn, LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        addView(row, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));

        View divider = new View(context);
        divider.setBackgroundColor(AndroidUtilities.adjustAlpha(Theme.getColor(Theme.SUB_TEXT_COLOR), 0.15f));
        addView(divider, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM, AndroidUtilities.dp12, 0, 0, 0));

        setOnClickListener(v -> {
            if (conversation != null && this.listener != null) {
                this.listener.onClick(conversation);
            }
        });
        setOnLongClickListener(v -> {
            if (conversation != null && this.listener != null) {
                this.listener.onLongClick(conversation);
                return true;
            }
            return false;
        });
    }

    public void setConversation(Conversation c) {
        this.conversation = c;
        String displayName = c.displayName != null ? c.displayName : c.address;
        if (displayName == null || displayName.equals(c.address)) {
            displayName = ContactHelper.resolveDisplayName(getContext(), c.address);
        }
        nameView.setText(displayName);
        avatarView.bind(c.address, displayName);
        previewView.setText(formatPreview(c));
        timeView.setText(formatTime(c.lastTimestamp));
        applyColors(c);
        applySelectionVisuals();
    }

    public void setSelectionState(boolean selectionMode, boolean selected) {
        this.selectionMode = selectionMode;
        this.selected = selected;
        applySelectionVisuals();
    }

    private void applySelectionVisuals() {
        if (selectionMode) {
            avatarView.setVisibility(GONE);
            checkCircle.setVisibility(VISIBLE);
            updateCheckCircle(selected);
            checkMark.setVisibility(selected ? VISIBLE : GONE);
            unreadDot.setVisibility(GONE);
            badgeView.setVisibility(GONE);
            setBackgroundColor(selected
                    ? Theme.getColor(Theme.SELECTED_BACKGROUND_COLOR)
                    : Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
        } else if (conversation != null) {
            avatarView.setVisibility(VISIBLE);
            checkCircle.setVisibility(GONE);
            checkMark.setVisibility(GONE);
            applyColors(conversation);
        }
    }

    private void updateCheckCircle(boolean checked) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        if (checked) {
            circle.setColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        } else {
            circle.setColor(0x00000000);
            circle.setStroke(AndroidUtilities.dp2, Theme.getColor(Theme.SUB_TEXT_COLOR));
        }
        checkCircle.setBackground(circle);
    }

    private String formatPreview(Conversation c) {
        if (c.isEncrypted) {
            return LocaleController.getString("encrypted_message");
        }
        if (c.preview != null && !c.preview.trim().isEmpty()) {
            return c.preview.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        }
        Message latest = AppDatabase.getInstance(getContext()).messageDao().getLatestMessage(c.id);
        if (latest != null && latest.body != null && !latest.body.trim().isEmpty()) {
            return latest.body.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        }
        return "";
    }

    private void applyColors(Conversation c) {
        Typeface regular = Theme.getTypeface(Theme.TypeFaceKey.DEFAULT_TYPEFACE);
        Typeface bold = Typeface.create(regular, Typeface.BOLD);
        boolean unread = c.unreadCount > 0;

        nameView.setTextColor(Theme.getColor(Theme.TEXT_COLOR));
        previewView.setTextColor(AndroidUtilities.adjustAlpha(Theme.getColor(Theme.TEXT_COLOR), 0.65f));
        timeView.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        setBackgroundColor(Theme.getColor(Theme.CARD_BACKGROUND_COLOR));
        if (c.isSpam) {
            nameView.setTextColor(Theme.getColor(Theme.SPAM_BADGE_COLOR));
        }

        unreadDot.setVisibility(unread ? VISIBLE : GONE);
        nameView.setTypeface(unread ? bold : regular);
        previewView.setTypeface(regular);

        if (unread) {
            badgeView.setVisibility(VISIBLE);
            badgeView.setText(String.valueOf(c.unreadCount));
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
            badgeView.setBackground(bg);
            badgeView.setTextColor(Theme.getColor(Theme.ACTIONBAR_TEXT_COLOR));
        } else {
            badgeView.setVisibility(GONE);
        }
    }

    private String formatTime(long ts) {
        if (ts <= 0) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(ts));
    }
}

package com.dpouya.aitexty.ui.cells;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.provider.Telephony;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dpouya.aitexty.components.FaIconView;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.FontAwesome;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.LinkHelper;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.Theme;

public class MessageBubbleCell extends FrameLayout {
    public interface SpeakListener {
        void onSpeak(Message message);
    }

    private final LinearLayout row;
    private final FaIconView speakBtn;
    private final FrameLayout bubble;
    private final TextView bodyView;
    private final TextView timeView;
    private SpeakListener speakListener;

    public MessageBubbleCell(Context context) {
        super(context);
        setPadding(AndroidUtilities.dp5, AndroidUtilities.dp2, AndroidUtilities.dp5, AndroidUtilities.dp2);

        row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        speakBtn = new FaIconView(context, 16);
        speakBtn.setIcon(FontAwesome.Icon.VOLUME_UP);
        speakBtn.setIconColor(Theme.getColor(Theme.ACTIONBAR_COLOR));
        speakBtn.setVisibility(GONE);
        speakBtn.setContentDescription(LocaleController.getString("speak_button"));

        bubble = new FrameLayout(context);
        bodyView = new TextView(context);
        bodyView.setTextSize(15);
        bodyView.setPadding(AndroidUtilities.dp10, AndroidUtilities.dp7, AndroidUtilities.dp10, AndroidUtilities.dp7);
        bubble.addView(bodyView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        timeView = new TextView(context);
        timeView.setTextSize(10);

        addView(row, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    public void setSpeakListener(SpeakListener speakListener) {
        this.speakListener = speakListener;
    }

    public void setMessage(Message message, int incomingColor, int outgoingColor, boolean showSpeakButton) {
        boolean incoming = message.type == Telephony.Sms.MESSAGE_TYPE_INBOX;
        LinkHelper.apply(bodyView, message.body, Theme.getColor(Theme.ACTIONBAR_COLOR));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        timeView.setText(sdf.format(new java.util.Date(message.timestamp)));
        timeView.setTextColor(Theme.getColor(Theme.SUB_TEXT_COLOR));
        bodyView.setTextColor(Theme.getColor(Theme.TEXT_COLOR));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp15);
        bg.setColor(incoming ? incomingColor : outgoingColor);
        bodyView.setBackground(bg);

        speakBtn.setVisibility(showSpeakButton ? VISIBLE : GONE);
        speakBtn.setOnClickListener(v -> {
            if (speakListener != null) {
                speakListener.onSpeak(message);
            }
        });

        row.removeAllViews();
        LinearLayout bubbleColumn = new LinearLayout(getContext());
        bubbleColumn.setOrientation(LinearLayout.VERTICAL);
        bubbleColumn.addView(bubble, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        bubbleColumn.addView(timeView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                incoming
                        ? (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT)
                        : (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT),
                4, 2, 0, 0));

        if (incoming) {
            row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            row.addView(speakBtn, LayoutHelper.createLinear(32, 32, Gravity.CENTER_VERTICAL, 0, 0, 4, 0));
            row.addView(bubbleColumn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        } else {
            row.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            row.addView(bubbleColumn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
            row.addView(speakBtn, LayoutHelper.createLinear(32, 32, Gravity.CENTER_VERTICAL, 4, 0, 0, 0));
        }
    }
}

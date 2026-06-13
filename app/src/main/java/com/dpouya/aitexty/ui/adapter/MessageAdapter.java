package com.dpouya.aitexty.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.accessibility.AccessibilitySettings;
import com.dpouya.aitexty.data.entity.Message;
import com.dpouya.aitexty.ui.cells.MessageBubbleCell;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {
    private final Context context;
    private List<Message> items = new ArrayList<>();
    private int incomingColor;
    private int outgoingColor;
    private MessageBubbleCell.SpeakListener speakListener;
    private boolean showSpeakButton = AccessibilitySettings.isSpeakButtonEnabled();

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void setSpeakListener(MessageBubbleCell.SpeakListener speakListener) {
        this.speakListener = speakListener;
    }

    public void setShowSpeakButton(boolean showSpeakButton) {
        this.showSpeakButton = showSpeakButton;
        notifyDataSetChanged();
    }

    public void setItems(List<Message> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Message> getItems() {
        return items;
    }

    public void setBubbleColors(int incoming, int outgoing) {
        this.incomingColor = incoming;
        this.outgoingColor = outgoing;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MessageBubbleCell cell = new MessageBubbleCell(context);
        cell.setSpeakListener(message -> {
            if (speakListener != null) {
                speakListener.onSpeak(message);
            }
        });
        return new VH(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MessageBubbleCell cell = (MessageBubbleCell) holder.itemView;
        cell.setMessage(items.get(position), incomingColor, outgoingColor, showSpeakButton);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(MessageBubbleCell cell) {
            super(cell);
        }
    }
}

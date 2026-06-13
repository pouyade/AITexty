package com.dpouya.aitexty.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.ui.cells.ConversationCell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {
    public interface Listener extends ConversationCell.Listener {
        default void onSelectionChanged(int count) {
        }
    }

    private final Context context;
    private final Listener listener;
    private List<Conversation> items = new ArrayList<>();
    private boolean selectionMode;
    private final Set<Long> selectedIds = new HashSet<>();

    public ConversationAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<Conversation> items) {
        this.items = items != null ? items : new ArrayList<>();
        if (selectionMode) {
            selectedIds.retainAll(getVisibleIds(this.items));
            notifySelectionChanged();
        }
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public int getSelectedCount() {
        return selectedIds.size();
    }

    public List<Conversation> getSelectedConversations() {
        List<Conversation> selected = new ArrayList<>();
        for (Conversation c : items) {
            if (selectedIds.contains(c.id)) {
                selected.add(c);
            }
        }
        return selected;
    }

    public void enterSelectionMode(long conversationId) {
        selectionMode = true;
        selectedIds.clear();
        selectedIds.add(conversationId);
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void exitSelectionMode() {
        selectionMode = false;
        selectedIds.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void toggleSelection(long conversationId) {
        if (!selectionMode) {
            enterSelectionMode(conversationId);
            return;
        }
        if (selectedIds.contains(conversationId)) {
            selectedIds.remove(conversationId);
            if (selectedIds.isEmpty()) {
                exitSelectionMode();
                return;
            }
        } else {
            selectedIds.add(conversationId);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void selectAll() {
        if (!selectionMode) {
            return;
        }
        selectedIds.clear();
        for (Conversation c : items) {
            selectedIds.add(c.id);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    private Set<Long> getVisibleIds(List<Conversation> conversations) {
        Set<Long> ids = new HashSet<>();
        for (Conversation c : conversations) {
            ids.add(c.id);
        }
        return ids;
    }

    private void notifySelectionChanged() {
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConversationCell cell = new ConversationCell(context, listener);
        cell.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new VH(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Conversation conversation = items.get(position);
        ConversationCell cell = (ConversationCell) holder.itemView;
        cell.setConversation(conversation);
        cell.setSelectionState(selectionMode, selectedIds.contains(conversation.id));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(ConversationCell cell) {
            super(cell);
        }
    }
}

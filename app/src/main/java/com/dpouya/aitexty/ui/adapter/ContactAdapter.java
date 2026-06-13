package com.dpouya.aitexty.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.data.ContactEntry;
import com.dpouya.aitexty.ui.cells.ContactCell;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.VH> {
    public interface Listener extends ContactCell.Listener {
    }

    private final Context context;
    private final Listener listener;
    private List<ContactEntry> items = new ArrayList<>();

    public ContactAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<ContactEntry> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactCell cell = new ContactCell(context, listener);
        cell.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return new VH(cell);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ((ContactCell) holder.itemView).setContact(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(ContactCell cell) {
            super(cell);
        }
    }
}

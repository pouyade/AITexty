package com.dpouya.aitexty.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.data.entity.BlockEntry;
import com.dpouya.aitexty.ui.cells.BlocklistCell;

import java.util.ArrayList;
import java.util.List;

public class BlocklistAdapter extends RecyclerView.Adapter<BlocklistAdapter.VH> {
    private final Context context;
    private final BlocklistCell.Listener listener;
    private List<BlockEntry> items = new ArrayList<>();

    public BlocklistAdapter(Context context, BlocklistCell.Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<BlockEntry> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(new BlocklistCell(context, listener));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ((BlocklistCell) holder.itemView).setEntry(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(BlocklistCell cell) {
            super(cell);
        }
    }
}

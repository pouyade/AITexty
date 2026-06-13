package com.dpouya.aitexty.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.ui.cells.SettingsCell;

import java.util.Arrays;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.VH> {
    public interface Listener {
        void onClick(String key);
    }

    private final Context context;
    private final Listener listener;
    private final List<String[]> items = Arrays.asList(
            new String[]{"theme", LocaleController.getString("night_mode")},
            new String[]{"language_fa", "فارسی"},
            new String[]{"language_en", "English"},
            new String[]{"blocklist", LocaleController.getString("blocklist")},
            new String[]{"privacy", LocaleController.getString("privacy")},
            new String[]{"accessibility", LocaleController.getString("accessibility")},
            new String[]{"ai", LocaleController.getString("ai_settings")}
    );

    public SettingsAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(new SettingsCell(context, listener::onClick));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String[] item = items.get(position);
        ((SettingsCell) holder.itemView).bind(item[0], item[1]);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        VH(SettingsCell cell) {
            super(cell);
        }
    }
}

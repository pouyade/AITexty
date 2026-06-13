package com.dpouya.aitexty.activities;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.NotificationCenter;
import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.SpacesItemDecoration;
import com.dpouya.aitexty.helper.Theme;
import com.dpouya.aitexty.privacy.BlocklistManager;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.adapter.BlocklistAdapter;

public class BlocklistActivity extends BaseActivity implements NotificationCenter.NotificationCenterDelegate {
    private BlocklistAdapter adapter;
    private BlocklistManager blocklistManager;

    @Override
    protected View initViews(Context context) {
        blocklistManager = BlocklistManager.getInstance(context);
        RecyclerView list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.addItemDecoration(new SpacesItemDecoration(AndroidUtilities.dp5, AndroidUtilities.dp5, AndroidUtilities.dp5, 0));
        adapter = new BlocklistAdapter(context, entry -> {
            blocklistManager.unblock(entry.phoneNumber);
            refresh();
        });
        list.setAdapter(adapter);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didBlocklistChanged);
        refresh();
        return list;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("blocklist"));
    }

    private void refresh() {
        adapter.setItems(blocklistManager.getAll());
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.didBlocklistChanged) refresh();
    }

    @Override
    protected void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didBlocklistChanged);
        super.onDestroy();
    }
}

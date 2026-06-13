package com.dpouya.aitexty.activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.components.BaseActivity;
import com.dpouya.aitexty.data.entity.Conversation;
import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LocaleController;
import com.dpouya.aitexty.helper.SpacesItemDecoration;
import com.dpouya.aitexty.privacy.HiddenChatManager;
import com.dpouya.aitexty.ui.ActionBar.ActionBar;
import com.dpouya.aitexty.ui.adapter.ConversationAdapter;

public class HiddenVaultActivity extends BaseActivity {
    private ConversationAdapter adapter;
    private HiddenChatManager hiddenManager;

    @Override
    protected View initViews(Context context) {
        hiddenManager = HiddenChatManager.getInstance(context);
        RecyclerView list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.addItemDecoration(new SpacesItemDecoration(AndroidUtilities.dp5, AndroidUtilities.dp5, AndroidUtilities.dp5, 0));
        adapter = new ConversationAdapter(context, new ConversationAdapter.Listener() {
            @Override
            public void onClick(Conversation conversation) {
                Intent i = new Intent(HiddenVaultActivity.this, ChatActivity.class);
                i.putExtra("conversation_id", conversation.id);
                startActivity(i);
            }

            @Override
            public void onLongClick(Conversation conversation) {
                hiddenManager.unhideConversation(conversation.id);
                adapter.setItems(hiddenManager.getHiddenConversations());
            }
        });
        adapter.setItems(hiddenManager.getHiddenConversations());
        list.setAdapter(adapter);
        return list;
    }

    @Override
    protected void initActionbar(ActionBar bar) {
        bar.showBackButton(true);
        bar.setOnIconClick(v -> finish());
        bar.setTitle(LocaleController.getString("hidden_chats"));
    }
}

package com.dpouya.aitexty.ui.ActionBar.Cell;

import android.content.Context;
import android.graphics.Typeface;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.helper.AndroidUtilities;
import com.dpouya.aitexty.helper.LayoutHelper;
import com.dpouya.aitexty.helper.SpacesItemDecoration;
import com.dpouya.aitexty.ui.ActionBar.ActionBarButton;
import com.dpouya.aitexty.ui.ActionBar.Adapter.ActionBarButtonListAdapter;


/**
 * Created by pouyadark on 2/23/19.
 */

public class ActionBarButtonListCell extends RecyclerView {
    private final Typeface typeface;
    private ActionBarButtonListAdapter adapter;

    public ActionBarButtonListCell(Context context, Typeface typeface) {
        super(context);
        this.typeface = typeface;
        init();
    }

    private void init() {
        setLayoutParams(LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.MATCH_PARENT));
        adapter = new ActionBarButtonListAdapter(getContext(),typeface);
        setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        addItemDecoration(new SpacesItemDecoration(AndroidUtilities.dp5,AndroidUtilities.dp5,0,0));
        setAdapter(adapter);

    }
    public void addItem(ActionBarButton actionbarButton){
        adapter.addItem(actionbarButton);
    }

    public void ClearButtons() {
        adapter.ClearButtons();
    }
}

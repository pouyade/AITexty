package com.dpouya.aitexty.ui.ActionBar.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.dpouya.aitexty.ui.ActionBar.ActionBarButton;
import com.dpouya.aitexty.ui.ActionBar.Cell.ActionBarButtonCell;

import java.util.ArrayList;


/**
 * Created by pouyadark on 2/23/19.
 */

public class ActionBarButtonListAdapter extends RecyclerView.Adapter<ActionBarButtonListAdapter.ViewHolder> {
    ArrayList<ActionBarButton> actionbarButtons=new ArrayList<>();
    Context mContext;
    Typeface typeface;

    public ActionBarButtonListAdapter(Context mContext, Typeface typeface) {
        this.mContext = mContext;
        this.typeface = typeface;
    }

    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        return new ViewHolder(new ActionBarButtonCell(mContext,typeface));
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
        ((ActionBarButtonCell)holder.itemView).setActionBarButton(actionbarButtons.get(position));
    }

    @Override
    public int getItemCount() {
        return actionbarButtons.size();
    }

    public void addItem(ActionBarButton actionbarButton){
        actionbarButtons.add(actionbarButton);
        notifyDataSetChanged();
    }

    public void ClearButtons() {
        actionbarButtons.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

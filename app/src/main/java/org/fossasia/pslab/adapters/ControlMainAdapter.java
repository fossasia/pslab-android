package org.fossasia.pslab.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.fossasia.pslab.R;

/**
 * Created by asitava on 6/6/17.
 */

public class ControlMainAdapter extends RecyclerView.Adapter<ControlMainAdapter.ViewHolder> {
    private String[] mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mCardView = (CardView) v.findViewById(R.id.cardview_control_main);
            mTextView = (TextView) v.findViewById(R.id.text_control_main1);
        }
    }

    public ControlMainAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    @Override
    public ControlMainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.control_main_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setIsRecyclable(true);
        holder.mTextView.setText(mDataset[position]);
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
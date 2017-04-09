package com.viveksb007.pslab.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.viveksb007.pslab.R;

/**
 * Created by akarshan on 4/9/17.
 */

public class ApplicationsAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private String[] applicationsList;
    private int[] imageList;

    public ApplicationsAdapter(Context context, String[] applicationsList, int[] imageList) {
        this.applicationsList = applicationsList;
        this.imageList = imageList;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return applicationsList.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        if (convertView == null) {
            rowView = inflater.inflate(R.layout.applications_list_item, null);
        } else {
            rowView = convertView;
        }
        holder.tv = (TextView) rowView.findViewById(R.id.application_tv);
        holder.img = (ImageView) rowView.findViewById(R.id.application_icon);
        holder.tv.setText(applicationsList[position]);
        holder.img.setImageResource(imageList[position]);
        return rowView;
    }

}

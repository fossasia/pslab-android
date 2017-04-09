package com.viveksb007.pslab.fragment;

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

public class CustomAdapter extends BaseAdapter {
    private static LayoutInflater inflater=null;
    private String [] applicationsList;
    private int [] imageList;
    public CustomAdapter(Context context, String[] list1, int[] list2) {
        applicationsList=list1;
        imageList = list2;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return applicationsList.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    private class Holder
    {
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.applications_list, null);
        holder.tv=(TextView) rowView.findViewById(R.id.application_tv);
        holder.img=(ImageView) rowView.findViewById(R.id.application_icon);
        holder.tv.setText(applicationsList[position]);
        holder.img.setImageResource(imageList[position]);
        return rowView;
    }

}

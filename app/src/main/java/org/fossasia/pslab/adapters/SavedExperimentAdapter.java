package org.fossasia.pslab.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.fossasia.pslab.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by viveksb007 on 8/7/17.
 */

public class SavedExperimentAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> experimentHeader;
    private HashMap<String, List<String>> experimentList;

    public SavedExperimentAdapter(Context context, List<String> experimentGroupHeader, HashMap<String, List<String>> experimentList) {
        this.context = context;
        this.experimentHeader = experimentGroupHeader;
        this.experimentList = experimentList;
    }

    @Override
    public int getGroupCount() {
        return this.experimentHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.experimentList.get(this.experimentHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.experimentHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.experimentList.get(this.experimentHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
        }
        TextView tvExperimentListHeader = (TextView) convertView.findViewById(android.R.id.text1);
        tvExperimentListHeader.setTypeface(null, Typeface.BOLD);
        tvExperimentListHeader.setText(headerTitle);
        TextView tvTemp = (TextView) convertView.findViewById(android.R.id.text2);
        tvTemp.setText("Some Description");
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String experimentName = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.experiment_list_item, null);
        }
        TextView tvExperimentTitle = (TextView) convertView.findViewById(R.id.exp_list_item);
        tvExperimentTitle.setText(experimentName);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

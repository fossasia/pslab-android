package io.pslab.adapters;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import io.pslab.R;

public class CustomAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listHeader;
    private HashMap<String,List<String>> listChild;
    public CustomAdapter(Context context, List<String> listHeader, HashMap<String, List<String>> listChild) {
        this.context = context;
        this.listHeader = listHeader;
        this.listChild = listChild;
    }

    @Override
    public int getGroupCount() {
        return listHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listChild.get(listHeader.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return listHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listChild.get(listHeader.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String question = (String) getGroup(i);
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_group,null);
        }

        TextView questionTextView = view.findViewById(R.id.question_textView);
        questionTextView.setText(question);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String answer = (String) getChild(i,i1);
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item,null);
        }

        TextView answerTextView = view.findViewById(R.id.lblListItem);
        answerTextView.setMovementMethod(LinkMovementMethod.getInstance());
        answerTextView.setText(Html.fromHtml(answer));

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}

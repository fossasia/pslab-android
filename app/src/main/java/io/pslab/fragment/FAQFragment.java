package io.pslab.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.pslab.R;

public class FAQFragment extends Fragment {


    private List<String> listHeader;
    private HashMap<String,List<String>> listChild;
    private ExpandableListView expandableListView;
    private FaqListAdapter faqListAdapter;

    public static FAQFragment newInstance() {
        return new FAQFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void prepareListData() {
        String[] allQuestion = getResources().getStringArray(R.array.faq_questions);
        String[] allAnswer = getResources().getStringArray(R.array.faq_answers);

        listHeader = new ArrayList<>();
        listChild = new HashMap<>();

        for (int i = 0; i < allQuestion.length; i++) {
            listHeader.add(allQuestion[i]);
            List<String> child = new ArrayList<>();
            child.add(allAnswer[i]);
            listChild.put(listHeader.get(i),child);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_faq, container, false);
        expandableListView = rootView.findViewById(R.id.expListView);
        prepareListData();
        faqListAdapter = new FaqListAdapter(getContext(),listHeader,listChild);
        expandableListView.setAdapter(faqListAdapter);
        return rootView;
    }

    public class FaqListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private List<String> listHeader;
        private HashMap<String,List<String>> listChild;
        public FaqListAdapter(Context context, List<String> listHeader, HashMap<String, List<String>> listChild) {
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


}

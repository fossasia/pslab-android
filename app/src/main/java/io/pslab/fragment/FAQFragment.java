package io.pslab.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import io.pslab.R;

public class FAQFragment extends Fragment {

    private String[] questions;
    private String[][] answers;

    public static FAQFragment newInstance() {
        return new FAQFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        questions = new String[]{getString(R.string.faq_question1), getString(R.string.faq_question2), getString(R.string.faq_question3), getString(R.string.faq_question4)};

        answers = new String[][]{
                {getString(R.string.faq_answer1)},
                {getString(R.string.faq_answer2)},
                {getString(R.string.faq_answer3)},
                {getString(R.string.faq_answer4)}
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_faq, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ExpandableListView listView;

        listView = (ExpandableListView) view.findViewById(R.id.expListView);
        listView.setAdapter(new ExpandableListAdapter(questions, answers));
        listView.setGroupIndicator(null);

    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private final LayoutInflater inf;
        private String[] questions;
        private String[][] answers;

        public ExpandableListAdapter(String[] questions, String[][] answers) {
            this.questions = questions;
            this.answers = answers;
            inf = LayoutInflater.from(getActivity());
        }

        @Override
        public int getGroupCount() {
            return questions.length;
        }

        @Override
        public int getChildrenCount(int questionPosition) {
            return answers[questionPosition].length;
        }

        @Override
        public Object getGroup(int questionPosition) {
            return questions[questionPosition];
        }

        @Override
        public Object getChild(int questionPosition, int answerPosition) {
            return answers[questionPosition][answerPosition];
        }

        @Override
        public long getGroupId(int questionPosition) {
            return questionPosition;
        }

        @Override
        public long getChildId(int questionPosition, int answerPosition) {
            return answerPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getChildView(int questionPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ViewHolder holder;
            View v = convertView;
            if (v == null) {
                v = inf.inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder();

                holder.text = (TextView) v.findViewById(R.id.lblListItem);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.text.setText(getChild(questionPosition, childPosition).toString());

            return v;
        }

        @Override
        public View getGroupView(int questionPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;
            View v = convertView;
            if (v == null) {
                v = inf.inflate(R.layout.list_group, parent, false);

                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(R.id.lblListHeader);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            holder.text.setText(getGroup(questionPosition).toString());

            return v;
        }

        @Override
        public boolean isChildSelectable(int questionPosition, int answerPosition) {
            return true;
        }

        private class ViewHolder {
            private TextView text;
        }
    }
}

package io.pslab.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.pslab.R;
import io.pslab.adapters.FaqListAdapter;

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


}

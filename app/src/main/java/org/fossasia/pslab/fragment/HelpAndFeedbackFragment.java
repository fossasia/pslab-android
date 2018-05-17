package org.fossasia.pslab.fragment;


import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Harsh on 16-05-2018.
 */

public class HelpAndFeedbackFragment extends Fragment {

    public static HelpAndFeedbackFragment newInstance() {
        return new HelpAndFeedbackFragment();
    }

    @BindView(R.id.appBarAnim)
    AppBarLayout appBarLayout;

    public HelpAndFeedbackFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_help_feedback, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        return super.onOptionsItemSelected(item);
    }

}

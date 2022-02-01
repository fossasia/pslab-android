package io.pslab.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import org.jetbrains.annotations.NotNull;

import io.pslab.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HelpAndFeedbackFragment extends Fragment {

    @BindView(R.id.appBarAnim)
    AppBarLayout appBarLayout;

    public static HelpAndFeedbackFragment newInstance() {
        return new HelpAndFeedbackFragment();
    }

    public HelpAndFeedbackFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_feedback, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}

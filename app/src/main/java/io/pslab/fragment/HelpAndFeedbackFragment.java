package io.pslab.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import io.pslab.databinding.FragmentHelpFeedbackBinding;

public class HelpAndFeedbackFragment extends Fragment {

    private FragmentHelpFeedbackBinding binding;

    public static HelpAndFeedbackFragment newInstance() {
        return new HelpAndFeedbackFragment();
    }

    public HelpAndFeedbackFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHelpFeedbackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

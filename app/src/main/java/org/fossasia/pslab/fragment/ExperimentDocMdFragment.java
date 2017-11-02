package org.fossasia.pslab.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;

import br.tiagohm.markdownview.MarkdownView;

/**
 * Created by asitava on 2/9/17.
 */

public class ExperimentDocMdFragment extends Fragment {

    private String mdFile;
    private MarkdownView mMarkdownView;

    public static ExperimentDocMdFragment newInstance(String mdFile) {
        ExperimentDocMdFragment experimentDocMdFragment = new ExperimentDocMdFragment();
        experimentDocMdFragment.mdFile = mdFile;
        return experimentDocMdFragment;
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experiment_doc_md, container, false);
        mMarkdownView = (MarkdownView) view.findViewById(R.id.perform_experiment_md);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMarkdownView.loadMarkdownFromAsset("DOC_HTML/apps/" + mdFile);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();

        ((PSLabApplication)getActivity().getApplication()).refWatcher.watch(this, ExperimentDocMdFragment.class.getSimpleName());
    }

}

package org.fossasia.pslab.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.fossasia.pslab.PSLabApplication;
import org.fossasia.pslab.R;
import org.fossasia.pslab.others.TouchImageView;

import br.tiagohm.markdownview.MarkdownView;

/**
 * Created by ThisIsNSH on 4/15/2018.
 */

public class ExperimentDocMdFragmentNew extends Fragment {

    private String mdFileTop, mdFileBottom, jpgPath;
    private MarkdownView mMarkdownTop, mMarkdownBottom;
    private TouchImageView schematicJpg;

    public static org.fossasia.pslab.fragment.ExperimentDocMdFragmentNew newInstance(String mdFileTop, String mdFileBottom, String jpgPath) {
        org.fossasia.pslab.fragment.ExperimentDocMdFragmentNew experimentDocMdFragment = new org.fossasia.pslab.fragment.ExperimentDocMdFragmentNew();
        experimentDocMdFragment.mdFileTop = mdFileTop;
        experimentDocMdFragment.mdFileBottom = mdFileBottom;
        experimentDocMdFragment.jpgPath = jpgPath;
        return experimentDocMdFragment;
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.experiment_doc_md_new, container, false);
        mMarkdownTop = (MarkdownView) view.findViewById(R.id.perform_experiment_top);
        mMarkdownBottom = (MarkdownView) view.findViewById(R.id.perform_experiment_bottom);
        schematicJpg = (TouchImageView) view.findViewById(R.id.schematic);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Resources res = getResources();
        int resID = res.getIdentifier(jpgPath, "drawable", getActivity().getPackageName());
        schematicJpg.setImageResource(resID);
        mMarkdownTop.loadMarkdownFromAsset("DOC_HTML/apps/" + mdFileTop);
        mMarkdownBottom.loadMarkdownFromAsset("DOC_HTML/apps/" + mdFileBottom);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((PSLabApplication) getActivity().getApplication()).refWatcher.watch(this, org.fossasia.pslab.fragment.ExperimentDocMdFragmentNew.class.getSimpleName());
    }
}

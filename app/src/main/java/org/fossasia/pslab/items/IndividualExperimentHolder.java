package org.fossasia.pslab.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import org.fossasia.pslab.R;

/**
 * Created by Padmal on 7/30/17.
 */

public class IndividualExperimentHolder extends TreeNode.BaseNodeViewHolder<IndividualExperimentHolder.IndividualExperiment> {

    public IndividualExperimentHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(TreeNode node, IndividualExperiment experiment) {

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.saved_experiments_individual_experiment_holder, null, false);

        TextView experimentName = (TextView) view.findViewById(R.id.individual_experiment_title);
        experimentName.setText(experiment.label);

        return view;
    }

    @Override
    public void toggle(boolean active) {
        /* Toggle not required; */
    }


    public static class IndividualExperiment {

        public String label;

        public IndividualExperiment(String label) {
            this.label = label;
        }
    }

}

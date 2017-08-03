package org.fossasia.pslab.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;

import org.fossasia.pslab.R;

/**
 * Created by Padmal on 7/30/17.
 */

public class ExperimentHeaderHolder extends TreeNode.BaseNodeViewHolder<ExperimentHeaderHolder.ExperimentHeader> {

    private ImageView arrow;
    private Space space;

    public ExperimentHeaderHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, ExperimentHeader header) {

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.saved_experiments_header_holder, null, false);

        TextView title = (TextView) view.findViewById(R.id.experiment_header_title);
        title.setText(header.title);

        arrow = (ImageView) view.findViewById(R.id.experiment_arrow);
        space = (Space) view.findViewById(R.id.experiment_holder_separator);
        int Visibility = header.level == 1 ? View.VISIBLE : View.GONE;
        space.setVisibility(Visibility);

        return view;
    }

    @Override
    public void toggle(boolean active) {
        arrow.setImageResource(active ? R.drawable.ic_arrow_drop_up_black_24dp : R.drawable.ic_arrow_drop_down_black_24dp);
    }

    public static class ExperimentHeader {

        public String title;
        public int level;

        public ExperimentHeader(String title, int level) {
            this.title = title;
            this.level = level;
        }

    }

}

package io.pslab.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.pslab.CheckBoxGetter;
import io.pslab.R;


public class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.CheckBoxHolder> {

    private Context boxcontext;
    private List<CheckBoxGetter> list = new ArrayList<>();

    public CheckBoxAdapter(Context boxcontext, List<CheckBoxGetter> list) {
        this.boxcontext = boxcontext;
        this.list = list;
    }

    @Override
    public CheckBoxHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(boxcontext).inflate(R.layout.item_checkbox, parent, false);
        return new CheckBoxHolder(view);
    }

    @Override
    public void onBindViewHolder(final CheckBoxHolder holder, final int position) {

        final CheckBoxGetter check = list.get(position);

        holder.tv_name.setText(check.getName());

        holder.checkBox.setChecked(check.isSelected());
        holder.checkBox.setTag(list.get(position));

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBoxGetter check1 = (CheckBoxGetter) holder.checkBox.getTag();
                check1.setSelected(holder.checkBox.isChecked());
                list.get(position).setSelected(holder.checkBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public List<CheckBoxGetter> getCheckList() {
        return list;
    }

    public static class CheckBoxHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private CheckBox checkBox;

        public CheckBoxHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_checkbox);
            checkBox = itemView.findViewById(R.id.checkBox_select);
        }
    }
}

package io.pslab.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.pslab.R;
import io.pslab.items.ApplicationItem;


public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.Holder> {

    private final List<ApplicationItem> applicationList;
    private final OnItemClickListener listener;


    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    /**
     * View holder for application list item
     */
    public static class Holder extends RecyclerView.ViewHolder {

        private final TextView header;
        private final TextView description;
        private final ImageView applicationIcon;

        public Holder(@NonNull final View itemView) {
            super(itemView);
            this.header = itemView.findViewById(R.id.heading_card);
            this.description = itemView.findViewById(R.id.description_card);
            this.applicationIcon = itemView.findViewById(R.id.application_icon);
        }

        public void setup(@NonNull final ApplicationItem applicationItem, @NonNull final OnItemClickListener listener) {
            header.setText(applicationItem.getApplicationName());
            description.setText(applicationItem.getApplicationDescription());
            applicationIcon.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            applicationIcon.setImageResource(applicationItem.getApplicationIcon());

            itemView.setOnClickListener(v -> listener.onItemClick(applicationItem));
        }
    }

    public ApplicationAdapter(@NonNull final List<ApplicationItem> applicationList, @NonNull final OnItemClickListener listener) {
        this.applicationList = applicationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.application_list_item, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        holder.setup(applicationList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ApplicationItem item);
    }

}

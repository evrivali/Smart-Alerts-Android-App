package com.example.smartalerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AlarmRecyclerViewAdapter extends RecyclerView.Adapter<AlarmRecyclerViewAdapter.MyViewHolder> {
    Context context;
    ArrayList<AlertViewModel> alertViewModels;
    private final View.OnClickListener onClickListener;

    public AlarmRecyclerViewAdapter(Context context, ArrayList<AlertViewModel> alertViewModels, View.OnClickListener onClickListener) {
        this.context = context;
        this.alertViewModels = alertViewModels;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public AlarmRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create View
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.recycler_view_row, parent, false);

        // Set onClickListener
        view.setOnClickListener(onClickListener);
        return new AlarmRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmRecyclerViewAdapter.MyViewHolder holder, int position) {
        // Set Data to view
        holder.textViewLevel.setText(Integer.toString(alertViewModels.get(position).getLevel()));
        holder.textViewType.setText(alertViewModels.get(position).getType());
        holder.textViewLocation.setText(alertViewModels.get(position).getLocation());
        holder.textViewTimestamp.setText(alertViewModels.get(position).getTimestamp());
        holder.itemView.setTag(alertViewModels.get(position).getId());

        int level = alertViewModels.get(position).getLevel();
        if (level == 0) {
            holder.imageView.setImageResource(R.drawable.ic_baseline_warning_24_green);
        } else if (level == 1) {
            holder.imageView.setImageResource(R.drawable.ic_baseline_warning_24_yellow);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_baseline_warning_24_red);
        }


    }

    @Override
    public int getItemCount() {
        return alertViewModels.size();
    }

    // This needs to match our row view
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewLevel;
        TextView textViewType;
        TextView textViewLocation;
        TextView textViewTimestamp;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewLocation = itemView.findViewById(R.id.textViewTypeLoc);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}

package ftn.project.presentation.adapter; // prilagodi prema svom paketu

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ftn.project.R;

public class HoursAdapter extends RecyclerView.Adapter<HoursAdapter.HourViewHolder> {

    private final String[] hours;

    public HoursAdapter() {
        // Kreira niz sati 00:00, 01:00 ... 23:00
        hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
        }
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_hour, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        holder.bind(hours[position]);
    }

    @Override
    public int getItemCount() {
        return hours.length;
    }

    static class HourViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvHour;

        public HourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
        }

        public void bind(String hour) {
            tvHour.setText(hour);
        }
    }
}

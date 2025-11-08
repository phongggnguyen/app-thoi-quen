package com.example.myhabits.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitType;

import java.util.ArrayList;
import java.util.List;

public class HabitReportListAdapter extends RecyclerView.Adapter<HabitReportListAdapter.ViewHolder> {
    private Context context;
    private List<Habit> habits;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Habit habit);
    }

    public HabitReportListAdapter(Context context) {
        this.context = context;
        this.habits = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvHabitName.setText(habit.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(habit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName;

        ViewHolder(View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
        }
    }
}
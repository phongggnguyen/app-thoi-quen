package com.example.myhabits.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.activities.HabitDetail;
import com.example.myhabits.models.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private List<Habit> habits;
    private final Context context;
    private static final int REQUEST_CODE_DETAIL = 100;

    public HabitAdapter(Context context) {
        this.context = context;
        this.habits = new ArrayList<>();
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.bind(habit);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHabitName;
        private final Button btnView;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            btnView = itemView.findViewById(R.id.btnView);
        }

        public void bind(Habit habit) {
            tvHabitName.setText(habit.getName());

            btnView.setOnClickListener(v -> {
                Intent intent = new Intent(context, HabitDetail.class);
                intent.putExtra("habit_id", habit.getId());
                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_DETAIL);
            });
        }
    }
}
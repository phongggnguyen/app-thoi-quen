package com.example.myhabits.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.activities.HabitDetailStatusDialog;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitStatusAdapter extends RecyclerView.Adapter<HabitStatusAdapter.ViewHolder> {
    private Context context;
    private List<Habit> habits;
    private boolean isCompleted;
    private String currentDate;
    private DailyStatusDao dailyStatusDao;

    public HabitStatusAdapter(Context context, boolean isCompleted, String currentDate) {
        this.context = context;
        this.habits = new ArrayList<>();
        this.isCompleted = isCompleted;
        this.currentDate = currentDate;
        this.dailyStatusDao = DBManager.getInstance(context).getDailyStatusDao();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_report_habit_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvHabitName.setText(habit.getName());

        if (isCompleted) {
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatus.setColorFilter(
                    context.getColor(R.color.colorSuccess),
                    PorterDuff.Mode.SRC_IN
            );
        } else {
            holder.ivStatus.setImageResource(R.drawable.ic_pending);
            holder.ivStatus.setColorFilter(
                    context.getColor(R.color.colorPending),
                    PorterDuff.Mode.SRC_IN
            );
        }
        holder.itemView.setOnClickListener(v -> onItemClick(habit));
    }

    private void onItemClick(Habit habit) {
        DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), currentDate);

        HabitDetailStatusDialog dialog = HabitDetailStatusDialog.newInstance(habit, status, currentDate);
        dialog.setOnNoteUpdatedListener(() -> {
            // Reload data khi ghi chú được cập nhật
            notifyDataSetChanged();
        });
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "habit_detail");
    }
    @Override
    public int getItemCount() {
        return habits.size();
    }

    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStatus;
        TextView tvHabitName;

        ViewHolder(View itemView) {
            super(itemView);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
        }
    }
}
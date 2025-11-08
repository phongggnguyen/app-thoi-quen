package com.example.myhabits.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.utils.ToastUtils;

import java.util.List;

public class ListCategoryAdapter extends RecyclerView.Adapter<ListCategoryAdapter.ViewHolder> {
    private static final String TAG = "ListCategoryAdapter";

    private Context context;
    private List<HabitType> categories;
    private OnCategoryListener listener;
    private HabitDao habitDao;

    public interface OnCategoryListener {
        void onEdit(HabitType category);
        void onDelete(HabitType category);
        void onViewHabitDetail(long habitId);
    }

    public ListCategoryAdapter(Context context, List<HabitType> categories) {
        this.context = context;
        this.categories = categories;
        this.habitDao = DBManager.getInstance(context).getHabitDao();
    }

    public void setOnCategoryListener(OnCategoryListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitType category = categories.get(position);

        try {
            holder.tvCategoryName.setText(category.getName());
            holder.tvHabitCount.setText(String.format("%d thói quen", category.getHabitCount()));
            holder.viewCategoryColor.getBackground().setTint(Color.parseColor(category.getColor()));

            holder.tvHabitCount.setOnClickListener(v -> {
                List<Habit> habits = habitDao.getByType(category.getId());
                if (!habits.isEmpty()) {
                    showHabitsPopupMenu(v, habits);
                } else {
                    ToastUtils.showToast(context,
                            "Không có thói quen nào trong danh mục này");
                }
            });

            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(category);
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(category);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error binding category: " + e.getMessage());
        }
    }

    private void showHabitsPopupMenu(View view, List<Habit> habits) {
        PopupMenu popup = new PopupMenu(context, view);
        Menu menu = popup.getMenu();

        try {
            for (int i = 0; i < habits.size(); i++) {
                Habit habit = habits.get(i);
                menu.add(Menu.NONE, i, i, habit.getName());
            }

            popup.setOnMenuItemClickListener(item -> {
                int position = item.getItemId();
                if (position >= 0 && position < habits.size()) {
                    Habit selectedHabit = habits.get(position);
                    if (listener != null) {
                        listener.onViewHabitDetail(selectedHabit.getId());
                    }
                }
                return true;
            });

            popup.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing habits popup: " + e.getMessage());
            Toast.makeText(context, "Lỗi hiển thị danh sách thói quen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public void updateData(List<HabitType> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
        Log.d(TAG, "Updated categories list. New size: " + newCategories.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvHabitCount;
        View viewCategoryColor;
        ImageView btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvHabitCount = itemView.findViewById(R.id.tvHabitCount);
            viewCategoryColor = itemView.findViewById(R.id.viewCategoryColor);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
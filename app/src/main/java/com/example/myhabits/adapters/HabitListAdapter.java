package com.example.myhabits.adapters;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.example.myhabits.R;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitListAdapter extends RecyclerView.Adapter<HabitListAdapter.ViewHolder> {
    private static final String TAG = "HabitListAdapter";
    private Context context;
    private List<Habit> habits;
    private DailyStatusDao dailyStatusDao;
    private OnHabitClickListener listener;
    private List<Integer> expandedItems;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    public interface OnHabitClickListener {
        void onCheckboxClick(Habit habit, String date, boolean isChecked);
    }

    public HabitListAdapter(Context context, List<Habit> habits,
                            DailyStatusDao dailyStatusDao, OnHabitClickListener listener) {
        this.context = context;
        this.habits = habits;
        this.dailyStatusDao = dailyStatusDao;
        this.listener = listener;
        this.expandedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);

        // Set màu nền và viền
        boolean isEvenPosition = position % 2 == 0;
        int backgroundColor = isEvenPosition ? R.color.item_background_1 : R.color.item_background_2;
        int borderColor = isEvenPosition ? R.color.item_border_1 : R.color.item_border_2;

        MaterialCardView cardView = (MaterialCardView) holder.itemView;
        cardView.setCardBackgroundColor(context.getColor(backgroundColor));
        cardView.setStrokeColor(context.getColor(borderColor));
        cardView.setStrokeWidth(dpToPx(2));

        // Hiển thị thông tin cơ bản
        holder.tvName.setText(habit.getName());
        holder.tvStartDate.setText("Bắt đầu: " + formatDate(habit.getStartDate()));
        holder.tvTime.setText("Thời gian: " + habit.getStartTime() + " - " + habit.getEndTime());
        holder.tvReminder.setText("Nhắc nhở: " + formatReminderTime(habit.getReminderMinutes()));
        holder.tvTargetDays.setText("Mục tiêu: " + habit.getTargetDays() + " ngày");
        holder.tvMonth.setText("Tháng " + monthYearFormat.format(Calendar.getInstance().getTime()));

        // Cập nhật tiến độ
        updateProgress(holder, habit);

        // Thiết lập trạng thái mở rộng
        boolean isExpanded = expandedItems.contains(position);
        holder.expandableSection.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.btnExpand.setRotation(isExpanded ? 180 : 0);

        if (isExpanded) {
            setupCheckboxes(holder, habit, position, true);
        }

        holder.btnExpand.setOnClickListener(v -> toggleExpansion(holder, habit, position));
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private void toggleExpansion(@NonNull ViewHolder holder, Habit habit, int position) {
        boolean expanded = expandedItems.contains(position);

        if (expanded) {
            expandedItems.remove(Integer.valueOf(position));
            holder.expandableSection.setVisibility(View.GONE);
            holder.btnExpand.animate()
                    .rotation(0)
                    .setDuration(200)
                    .start();
        } else {
            expandedItems.add(position);
            holder.expandableSection.setVisibility(View.VISIBLE);
            holder.btnExpand.animate()
                    .rotation(180)
                    .setDuration(200)
                    .start();
            setupCheckboxes(holder, habit, position, true);
        }
    }

    private void setupCheckboxes(@NonNull ViewHolder holder, Habit habit, int position, boolean isExpanded) {
        holder.checkboxContainer.removeAllViews();

        List<String> daysToShow = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), habit.getTargetDays());

        // Số ô hiển thị phụ thuộc vào trạng thái expand
        int displayCount = isExpanded ? daysToShow.size() : Math.min(daysToShow.size(), 10);

        // Tính số hàng cần thiết (5 ô mỗi hàng)
        int numRows = (displayCount + 4) / 5;
        holder.checkboxContainer.setRowCount(numRows);

        // Tạo và thêm các checkbox
        for (int i = 0; i < displayCount; i++) {
            String date = daysToShow.get(i);
            View checkboxView = createCheckboxWithDay(habit, date);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 5, 1f);
            params.rowSpec = GridLayout.spec(i / 5);
            params.setMargins(8, 8, 8, 8);

            checkboxView.setLayoutParams(params);
            holder.checkboxContainer.addView(checkboxView);
        }
    }

    private View createCheckboxWithDay(Habit habit, String date) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkbox_day_item, null);
        TextView tvDay = view.findViewById(R.id.tvDay);
        CheckBox checkbox = view.findViewById(R.id.checkbox);

        // Lấy trạng thái của ngày
        DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
        int statusCode = status != null ? status.getStatus() : DatabaseConstants.CHECK_STATUS_PENDING;

        // Hiển thị số ngày
        String dayText = "Ngày " + DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date);
        tvDay.setText(dayText);

        try {
            Date checkDate = DateUtils.parseDate(date);
            Date startDate = DateUtils.parseDate(habit.getStartDate());
            Date today = DateUtils.truncateTime(new Date());

            boolean isPastDate = checkDate.before(today);
            boolean isFutureDate = checkDate.after(today);
            boolean isToday = DateUtils.isSameDay(date, DateUtils.getCurrentDate());
            boolean isAfterStartDate = !checkDate.before(startDate);

            // Xử lý trạng thái checkbox
            if (statusCode == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                checkbox.setChecked(true);
                checkbox.setEnabled(false);
                checkbox.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.checkGreen)));
            } else if (statusCode == DatabaseConstants.CHECK_STATUS_MISSED ||
                    (isPastDate && isAfterStartDate && statusCode == DatabaseConstants.CHECK_STATUS_PENDING)) {
                checkbox.setChecked(true);
                checkbox.setEnabled(false);
                checkbox.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.checkRed)));
            } else if (isToday && isAfterStartDate) {
                checkbox.setChecked(false);
                checkbox.setEnabled(true);
                checkbox.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.checkNormal)));
            } else {
                checkbox.setChecked(false);
                checkbox.setEnabled(false);
                checkbox.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.checkNormal)));
            }

            if (isFutureDate) {
                checkbox.setEnabled(false);
                checkbox.setChecked(false);
                checkbox.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.checkNormal)));
            }

            // Xử lý sự kiện click
            setupCheckboxClickListener(checkbox, habit, date);

        } catch (Exception e) {
            e.printStackTrace();
            checkbox.setEnabled(false);
        }

        return view;
    }


    private void setupCheckboxClickListener(CheckBox checkbox, Habit habit, String date) {
        if (checkbox.isEnabled()) {
            checkbox.setOnClickListener(v -> {
                boolean isChecked = checkbox.isChecked();
                if (listener != null) {
                    listener.onCheckboxClick(habit, date, isChecked);
                }
                if (isChecked) {
                    checkbox.setButtonTintList(ColorStateList.valueOf(
                            context.getColor(R.color.checkGreen)));
                    checkbox.setEnabled(false);
                }
            });
        }
    }

    private void updateProgress(@NonNull ViewHolder holder, Habit habit) {
        try {
            int totalBoxes = habit.getTargetDays();
            int completedBoxes = 0;

            List<String> daysToCheck = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), totalBoxes);

            for (String date : daysToCheck) {
                DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
                if (status != null && status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                    completedBoxes++;
                }
            }

            holder.tvProgress.setText(completedBoxes + "/" + totalBoxes);

            int progressPercent = totalBoxes > 0 ? (completedBoxes * 100) / totalBoxes : 0;
            if (progressPercent == 100 && habit.getStatus() != 1) {
                habit.setStatus(1);
                DBManager.getInstance(context).getHabitDao().update(habit);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating progress: " + e.getMessage());
            holder.tvProgress.setText("0/0");
        }
    }

    private String formatReminderTime(int minutes) {
        if (minutes >= 60) {
            int hours = minutes / 60;
            return hours + " giờ trước";
        }
        return minutes + " phút trước";
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String getFrequencyText(String frequency) {
        return "daily".equals(frequency) ? "Hằng ngày" : "Hằng tháng";
    }

    public void resetExpanded() {
        expandedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return habits != null ? habits.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStartDate, tvTime, tvReminder, tvTargetDays, tvProgress, tvMonth;
        GridLayout checkboxContainer;
        ImageView btnExpand;
        View expandableSection;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHabitName);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvReminder = itemView.findViewById(R.id.tvReminder);
            tvTargetDays = itemView.findViewById(R.id.tvTargetDays);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            checkboxContainer = itemView.findViewById(R.id.checkboxContainer);
            btnExpand = itemView.findViewById(R.id.btnExpand);
            expandableSection = itemView.findViewById(R.id.expandableSection);
        }
    }
}
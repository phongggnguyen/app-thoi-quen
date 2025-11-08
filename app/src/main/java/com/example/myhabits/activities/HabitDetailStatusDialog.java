package com.example.myhabits.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.myhabits.R;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.utils.DateUtils;
import com.example.myhabits.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitDetailStatusDialog extends DialogFragment {
    private Habit habit;
    private DailyStatus status;
    private String date;
    private DailyStatusDao dailyStatusDao;
    private OnNoteUpdatedListener listener;

    public interface OnNoteUpdatedListener {
        void onNoteUpdated();
    }

    public static HabitDetailStatusDialog newInstance(Habit habit, DailyStatus status, String date) {
        HabitDetailStatusDialog dialog = new HabitDetailStatusDialog();
        Bundle args = new Bundle();
        args.putParcelable("habit", habit);
        args.putParcelable("status", status);
        args.putString("date", date);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyStatusDao = DBManager.getInstance(requireContext()).getDailyStatusDao();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_habit_detail_status, null);

        if (getArguments() != null) {
            habit = getArguments().getParcelable("habit");
            status = getArguments().getParcelable("status");
            date = getArguments().getString("date");
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());

        View titleView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_title_with_close, null);
        TextView titleText = titleView.findViewById(R.id.dialogTitle);
        ImageView closeButton = titleView.findViewById(R.id.closeButton);

        titleText.setText(habit.getName());
        closeButton.setOnClickListener(v -> dismiss());

        setupViews(view);

        return builder
                .setCustomTitle(titleView)
                .setView(view)
                .create();
    }

    private void setupViews(View view) {
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvDayNumber = view.findViewById(R.id.tvDayNumber);
        TextView tvScheduleTime = view.findViewById(R.id.tvScheduleTime);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        TextView tvCompletedTime = view.findViewById(R.id.tvCompletedTime);
        EditText edtNote = view.findViewById(R.id.edtNote);
        Button btnSaveNote = view.findViewById(R.id.btnSaveNote);
        View layoutCompletedTime = view.findViewById(R.id.layoutCompletedTime);
        View layoutNote = view.findViewById(R.id.layoutNote);


        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date habitDate = DateUtils.parseDate(date);
            tvDate.setText(displayFormat.format(habitDate));
        } catch (Exception e) {
            tvDate.setText(date);
        }

        int dayNumber = DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date);
        tvDayNumber.setText(String.format("Ngày %d/%d", dayNumber, habit.getTargetDays()));

        tvScheduleTime.setText(String.format("%s - %s", habit.getStartTime(), habit.getEndTime()));

        // Kiểm tra ngày quá khứ
        boolean isPastDate = false;
        try {
            Date checkDate = DateUtils.parseDate(date);
            Date currentDate = DateUtils.parseDate(DateUtils.getCurrentDate());
            isPastDate = checkDate.before(currentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hiển thị trạng thái và các view tương ứng
        if (status != null) {
            if (status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                // Trạng thái hoàn thành
                tvStatus.setText("Đã hoàn thành");
                tvStatus.setTextColor(requireContext().getColor(R.color.colorSuccess));
                layoutCompletedTime.setVisibility(View.VISIBLE);
                layoutNote.setVisibility(View.GONE);

                if (status.getCheckTime() > 0) {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvCompletedTime.setText(timeFormat.format(new Date(status.getCheckTime())));
                }
            } else if (isPastDate || status.getStatus() == DatabaseConstants.CHECK_STATUS_MISSED) {
                // Trạng thái bỏ lỡ - cho ngày quá khứ hoặc đã đánh dấu bỏ lỡ
                tvStatus.setText("Đã bỏ lỡ");
                tvStatus.setTextColor(requireContext().getColor(R.color.colorError));
                layoutCompletedTime.setVisibility(View.GONE);
                layoutNote.setVisibility(View.VISIBLE);
            } else {
                // Trạng thái chưa hoàn thành
                tvStatus.setText("Chưa hoàn thành");
                tvStatus.setTextColor(requireContext().getColor(R.color.colorWarning));
                layoutCompletedTime.setVisibility(View.GONE);
                layoutNote.setVisibility(View.GONE);
            }

            // Hiển thị ghi chú nếu có
            if (status.getNote() != null) {
                edtNote.setText(status.getNote());
            }
        } else {
            // Chưa có status
            if (isPastDate) {
                // Ngày quá khứ -> Bỏ lỡ
                tvStatus.setText("Đã bỏ lỡ");
                tvStatus.setTextColor(requireContext().getColor(R.color.colorError));
                layoutCompletedTime.setVisibility(View.GONE);
                layoutNote.setVisibility(View.VISIBLE);
            } else {
                // Ngày hiện tại hoặc tương lai -> Chưa hoàn thành
                tvStatus.setText("Chưa hoàn thành");
                tvStatus.setTextColor(requireContext().getColor(R.color.colorWarning));
                layoutCompletedTime.setVisibility(View.GONE);
                layoutNote.setVisibility(View.GONE);
            }
        }

        // Chỉ cho phép chỉnh sửa ghi chú cho thói quen bỏ lỡ
        boolean canEditNote = isPastDate || (status != null && status.getStatus() == DatabaseConstants.CHECK_STATUS_MISSED);
        edtNote.setEnabled(canEditNote);
        btnSaveNote.setEnabled(canEditNote);

        if (!canEditNote) {
            btnSaveNote.setAlpha(0.5f);
        }

        // Xử lý lưu ghi chú
        btnSaveNote.setOnClickListener(v -> {
            if (canEditNote) {
                String note = edtNote.getText().toString().trim();
                saveNote(note);
            }
        });
    }

    private void saveNote(String note) {
        if (status == null) {
            // Tạo status mới nếu chưa có
            status = new DailyStatus(
                    habit.getId(),
                    date,
                    DatabaseConstants.CHECK_STATUS_MISSED, // Đánh dấu là đã bỏ lỡ khi thêm ghi chú
                    DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date)
            );
            status.setNote(note);
            status.setNoteUpdatedTime(System.currentTimeMillis());
            dailyStatusDao.insert(status);
        } else {
            // Cập nhật ghi chú
            status.setNote(note);
            status.setNoteUpdatedTime(System.currentTimeMillis());
            if (status.getStatus() == DatabaseConstants.CHECK_STATUS_PENDING) {
                status.setStatus(DatabaseConstants.CHECK_STATUS_MISSED);
            }
            dailyStatusDao.update(status);
        }

        ToastUtils.showToast(requireContext(), "Đã lưu ghi chú");
        if (listener != null) {
            listener.onNoteUpdated();
        }
    }

    public void setOnNoteUpdatedListener(OnNoteUpdatedListener listener) {
        this.listener = listener;
    }
}
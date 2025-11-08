package com.example.myhabits.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myhabits.R;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.utils.DateUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitReportDetail extends AppCompatActivity {
    private static final String TAG = "HabitReportDetail";

    private Toolbar toolbar;
    private TextView tvName;
    private TextView tvStartDate;
    private TextView tvTime;
    private TextView tvTargetDays;
    private TextView tvStatus;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private BarChart barChart;

    private Habit habit;
    private DailyStatusDao dailyStatusDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_report_detail);

        habit = getIntent().getParcelableExtra("habit");
        if (habit == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvName = findViewById(R.id.tvName);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvTime = findViewById(R.id.tvTime);
        tvTargetDays = findViewById(R.id.tvTargetDays);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        barChart = findViewById(R.id.barChart);

        dailyStatusDao = DBManager.getInstance(this).getDailyStatusDao();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết thói quen");
        }
    }

    private void loadData() {
        // Hiển thị thông tin cơ bản
        tvName.setText(habit.getName());

        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = DateUtils.parseDate(habit.getStartDate());
            tvStartDate.setText("Ngày bắt đầu: " + displayFormat.format(startDate));
        } catch (Exception e) {
            tvStartDate.setText("Ngày bắt đầu: " + habit.getStartDate());
        }

        tvTime.setText(String.format("Thời gian thực hiện: %s - %s",
                habit.getStartTime(), habit.getEndTime()));

        tvTargetDays.setText(String.format("Số ngày mục tiêu: %d ngày", habit.getTargetDays()));

        // Hiển thị trạng thái theo database
        String statusText;
        int statusColor;
        if (habit.getStatus() == 1) { // Đã hoàn thành
            statusText = "Đã hoàn thành";
            // statusColor = getColor(R.color.colorSuccess);
        } else { // Chưa hoàn thành (status == 0)
            statusText = "Chưa hoàn thành";
            //statusColor = getColor(R.color.colorWarning);
        }
        tvStatus.setText("Trạng thái: " + statusText);
        //tvStatus.setTextColor(statusColor);

        // Đánh dấu các ngày bỏ lỡ trước khi tính toán
        dailyStatusDao.markMissedDays(habit.getId());

        // Hiển thị tiến độ và biểu đồ
        updateProgress();
    }

    private void updateProgress() {
        try {
            int totalDays = habit.getTargetDays();
            int completedDays = 0;
            int missedDays = 0;
            int pendingDays = 0;

            List<String> daysToCheck = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), totalDays);

            for (String date : daysToCheck) {
                DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
                if (status != null) {
                    if (status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                        completedDays++;
                    } else if (status.getStatus() == DatabaseConstants.CHECK_STATUS_MISSED) {
                        missedDays++;
                    } else {
                        pendingDays++;
                    }
                } else {
                    if (DateUtils.isDatePassed(date)) {
                        missedDays++;
                    } else {
                        pendingDays++;
                    }
                }
            }

            // Cập nhật tiến độ
            float progressPercent = (float) completedDays / totalDays * 100;
            tvProgress.setText(String.format("Tiến độ: %d/%d ngày (%.1f%%)",
                    completedDays, totalDays, progressPercent, completedDays, missedDays, pendingDays));

            progressBar.setMax(totalDays);
            progressBar.setProgress(completedDays);

            // Cập nhật biểu đồ
            setupBarChart(completedDays, missedDays, pendingDays);

        } catch (Exception e) {
            Log.e(TAG, "Error calculating progress: " + e.getMessage());
            tvProgress.setText("Không thể tính toán tiến độ");
            progressBar.setProgress(0);
        }
    }

    private void setupBarChart(int completed, int missed, int pending) {
        // Tạo entries cho biểu đồ
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, completed));
        entries.add(new BarEntry(1, missed));
        entries.add(new BarEntry(2, pending));

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(
                getColor(R.color.colorSuccess),  // Xanh - Hoàn thành
                getColor(R.color.colorError),    // Đỏ - Bỏ lỡ
                getColor(R.color.colorWarning)   // Vàng - Chờ thực hiện
        );

        // Định dạng giá trị hiển thị trên cột
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        // Thiết lập trục X
        String[] labels = new String[]{"Hoàn thành", "Bỏ lỡ", "Chờ thực hiện"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelRotationAngle(0);

        // Thiết lập trục Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(12f);
        leftAxis.setDrawGridLines(true);

        // Tắt trục Y bên phải
        barChart.getAxisRight().setEnabled(false);

        // Tùy chỉnh chung cho biểu đồ
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBorders(false);
        barChart.setTouchEnabled(false);

        // Thêm padding
        barChart.setExtraBottomOffset(10f);
        barChart.setExtraTopOffset(10f);
        barChart.setExtraLeftOffset(10f);
        barChart.setExtraRightOffset(10f);

        // Animation khi hiển thị
        barChart.animateY(1000);
        barChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
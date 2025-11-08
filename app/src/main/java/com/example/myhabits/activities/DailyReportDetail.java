package com.example.myhabits.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.HabitStatusAdapter;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.utils.DateUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyReportDetail extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvDate;
    private TextView tvTotalHabits;
    private TextView tvCompletionStatus;
    private PieChart pieChart;
    private RecyclerView rvCompletedHabits;
    private RecyclerView rvPendingHabits;

    private HabitDao habitDao;
    private DailyStatusDao dailyStatusDao;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report_detail);

        currentDate = getIntent().getStringExtra("date");
        if (currentDate == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        initData();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDate = findViewById(R.id.tvDate);
        tvTotalHabits = findViewById(R.id.tvTotalHabits);
        tvCompletionStatus = findViewById(R.id.tvCompletionStatus);
        pieChart = findViewById(R.id.pieChart);
        rvCompletedHabits = findViewById(R.id.rvCompletedHabits);
        rvPendingHabits = findViewById(R.id.rvPendingHabits);

        setupRecyclerViews();
        setupPieChart();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết báo cáo");
        }
    }

    private void initData() {
        DBManager dbManager = DBManager.getInstance(this);
        habitDao = dbManager.getHabitDao();
        dailyStatusDao = dbManager.getDailyStatusDao();
    }

    private void setupRecyclerViews() {
        rvCompletedHabits.setLayoutManager(new LinearLayoutManager(this));
        rvPendingHabits.setLayoutManager(new LinearLayoutManager(this));

        rvCompletedHabits.setAdapter(new HabitStatusAdapter(this, true, currentDate));
        rvPendingHabits.setAdapter(new HabitStatusAdapter(this, false, currentDate));
    }

    private void setupPieChart() {
        // Tắt description
        pieChart.getDescription().setEnabled(false);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);

        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setFormSize(12f);
        pieChart.getLegend().setTextColor(Color.BLACK);
        pieChart.getLegend().setXEntrySpace(20f);
        pieChart.setRotationEnabled(false);
        pieChart.setTouchEnabled(false);
    }

    private List<Habit> filterHabitsByDate(List<Habit> habits, String date) {
        List<Habit> filtered = new ArrayList<>();
        for (Habit habit : habits) {
            try {
                Date habitStart = DateUtils.parseDate(habit.getStartDate());
                Date checkDate = DateUtils.parseDate(date);

                if (!checkDate.before(habitStart)) {
                    int daysPassed = DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date);
                    if (daysPassed <= habit.getTargetDays()) {
                        filtered.add(habit);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filtered;
    }

    private long getCurrentUserId() {
        try {
            return DBManager.getInstance(this).getUserDao().getFirstUser().getId();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void loadData() {
        List<Habit> habits = filterHabitsByDate(
                habitDao.getByUserId(getCurrentUserId()),
                currentDate
        );

        List<Habit> completedHabits = new ArrayList<>();
        List<Habit> missedHabits = new ArrayList<>();
        List<Habit> pendingHabits = new ArrayList<>();

        String today = DateUtils.getCurrentDate();

        for (Habit habit : habits) {
            DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), currentDate);

            // Kiểm tra nếu là thói quen của ngày đã qua
            boolean isPastDate = false;
            try {
                Date checkDate = DateUtils.parseDate(currentDate);
                Date currentDate = DateUtils.parseDate(today);
                isPastDate = checkDate.before(currentDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (status != null) {
                if (status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                    completedHabits.add(habit);
                } else if (isPastDate || status.getStatus() == DatabaseConstants.CHECK_STATUS_MISSED) {
                    // Nếu là ngày đã qua hoặc đã được đánh dấu bỏ lỡ
                    missedHabits.add(habit);
                } else {
                    pendingHabits.add(habit);
                }
            } else {
                // Nếu chưa có status nhưng là ngày đã qua thì cũng tính là bỏ lỡ
                if (isPastDate) {
                    missedHabits.add(habit);
                } else {
                    pendingHabits.add(habit);
                }
            }
        }

        updateUI(habits.size(), completedHabits.size(), missedHabits.size(), pendingHabits.size());

        ((HabitStatusAdapter) rvCompletedHabits.getAdapter()).setHabits(completedHabits);

        List<Habit> notCompletedHabits = new ArrayList<>();
        notCompletedHabits.addAll(missedHabits);
        notCompletedHabits.addAll(pendingHabits);
        ((HabitStatusAdapter) rvPendingHabits.getAdapter()).setHabits(notCompletedHabits);
    }

    private void updatePieChart(int completed, int missed, int pending) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, "Đã hoàn thành"));
        entries.add(new PieEntry(missed, "Bỏ lỡ"));
        entries.add(new PieEntry(pending, "Chưa hoàn thành"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                getColor(R.color.colorSuccess),
                getColor(R.color.colorError),
                getColor(R.color.colorWarning)
        );

        // Định dạng giá trị hiển thị trong biểu đồ
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Chỉ hiển thị giá trị khi value > 0
                return value > 0 ? String.valueOf((int) value) : "";
            }
        });
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setFormSize(12f);
        pieChart.getLegend().setTextColor(Color.BLACK);
        pieChart.getLegend().setXEntrySpace(20f);

        pieChart.invalidate();
    }

    private void updateUI(int total, int completed, int missed, int pending) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date date = DateUtils.parseDate(currentDate);
            tvDate.setText("Ngày " + displayFormat.format(date));
        } catch (Exception e) {
            tvDate.setText("Ngày " + currentDate);
        }

        tvTotalHabits.setText("Tổng số thói quen: " + total);
        tvCompletionStatus.setText(String.format("Trạng thái hoàn thành: %d/%d", completed, total));

        updatePieChart(completed, missed, pending);
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
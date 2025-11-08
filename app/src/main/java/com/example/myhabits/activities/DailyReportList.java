package com.example.myhabits.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.DailyReportListAdapter;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;
import com.example.myhabits.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class DailyReportList extends Fragment {
    private RecyclerView rvDailyReports;
    private TextView tvEmpty;
    private DailyReportListAdapter adapter;
    private List<String> dates = new ArrayList<>();
    private HabitDao habitDao;
    private UserDao userDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.daily_report_list, container, false);
        initViews(view);
        loadData();
        return view;
    }

    private void initViews(View view) {
        rvDailyReports = view.findViewById(R.id.rvDailyReports);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new DailyReportListAdapter(requireContext());
        adapter.setOnItemClickListener(date -> {
            // Start DailyReportDetail Activity
            Intent intent = new Intent(requireContext(), DailyReportDetail.class);
            intent.putExtra("date", date);
            startActivity(intent);
            requireActivity().overridePendingTransition(0, 0);
        });

        rvDailyReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDailyReports.setAdapter(adapter);

        DBManager dbManager = DBManager.getInstance(requireContext());
        habitDao = dbManager.getHabitDao();
        userDao = dbManager.getUserDao();
    }

    private void loadData() {
        List<Habit> habits = habitDao.getByUserId(userDao.getFirstUser().getId());
        Set<String> dateSet = new TreeSet<>(Collections.reverseOrder());
        String today = DateUtils.getCurrentDate();

        for (Habit habit : habits) {
            try {
                Date startDate = DateUtils.parseDate(habit.getStartDate());
                Date currentDate = DateUtils.parseDate(today);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                while (!calendar.getTime().after(currentDate)) {
                    dateSet.add(DateUtils.formatDate(calendar.getTime()));
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dates = new ArrayList<>(dateSet);

        if (dates.isEmpty()) {
            showEmptyView();
        } else {
            showData();
        }
    }

    private void showEmptyView() {
        rvDailyReports.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void showData() {
        rvDailyReports.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        adapter.setDates(dates);
    }
}
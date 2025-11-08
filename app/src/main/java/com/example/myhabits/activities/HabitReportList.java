package com.example.myhabits.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.HabitReportListAdapter;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;

import java.util.List;

public class HabitReportList extends Fragment {
    private RecyclerView rvHabitReports;
    private TextView tvEmpty;
    private HabitReportListAdapter adapter;
    private HabitDao habitDao;
    private UserDao userDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.habit_report_list, container, false);
        initViews(view);
        loadData();
        return view;
    }

    private void initViews(View view) {
        rvHabitReports = view.findViewById(R.id.rvHabitReports);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        adapter = new HabitReportListAdapter(requireContext());
        adapter.setOnItemClickListener(habit -> {
            Intent intent = new Intent(requireContext(), HabitReportDetail.class);
            intent.putExtra("habit", habit);
            startActivity(intent);
            requireActivity().overridePendingTransition(0, 0);
        });

        rvHabitReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHabitReports.setAdapter(adapter);

        DBManager dbManager = DBManager.getInstance(requireContext());
        habitDao = dbManager.getHabitDao();
        userDao = dbManager.getUserDao();
    }

    private void loadData() {
        // Sử dụng tham số includeArchived = true để lấy tất cả thói quen
        List<Habit> habits = habitDao.getByUserId(userDao.getFirstUser().getId(), true);

        if (habits.isEmpty()) {
            showEmptyView();
        } else {
            showData(habits);
        }
    }

    private void showEmptyView() {
        rvHabitReports.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void showData(List<Habit> habits) {
        rvHabitReports.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        adapter.setHabits(habits);
    }
}
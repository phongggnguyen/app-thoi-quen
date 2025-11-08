package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.ListCategoryAdapter;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class ListCategory extends AppCompatActivity {
    private static final String TAG = "ListCategory";
    private static final int REQUEST_CODE_HABIT_DETAIL = 101;

    private RecyclerView rvCategories;
    private TextView tvEmptyMessage;
    private ListCategoryAdapter adapter;
    private HabitTypeDao habitTypeDao;
    private HabitDao habitDao;
    private DBManager dbManager;
    private TextView tvTotalCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_category);

        initDatabase();
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadCategories();
    }

    private void initDatabase() {
        dbManager = DBManager.getInstance(this);
        if (dbManager == null) {
            Log.e(TAG, "Failed to initialize DBManager");
            ToastUtils.showToast(this, "Lỗi kết nối database");
            finish();
            return;
        }
        habitTypeDao = dbManager.getHabitTypeDao();
        habitDao = dbManager.getHabitDao();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvTotalCategories = findViewById(R.id.tvTotalCategories);

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            Intent intent = new Intent(this, Add_Update_Category.class);
            startActivityForResult(intent, MainActivity.RequestCode.ADD_CATEGORY);
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh mục");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ListCategoryAdapter(this, new ArrayList<>());
        adapter.setOnCategoryListener(new ListCategoryAdapter.OnCategoryListener() {
            @Override
            public void onEdit(HabitType category) {
                Intent intent = new Intent(ListCategory.this, Add_Update_Category.class);
                intent.putExtra("category_id", category.getId());
                startActivityForResult(intent, MainActivity.RequestCode.ADD_CATEGORY);
            }

            @Override
            public void onDelete(HabitType category) {
                showDeleteConfirmDialog(category);
            }

            @Override
            public void onViewHabitDetail(long habitId) {
                Intent intent = new Intent(ListCategory.this, HabitDetail.class);
                intent.putExtra("habit_id", habitId);
                startActivityForResult(intent, REQUEST_CODE_HABIT_DETAIL);
            }
        });
        rvCategories.setAdapter(adapter);
    }

    private void showDeleteConfirmDialog(HabitType category) {
        List<Habit> habits = habitDao.getByType(category.getId());
        if (!habits.isEmpty()) {
            ToastUtils.showToast(this,
                    "Không thể xóa danh mục đang có thói quen!");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa danh mục này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean deleted = habitTypeDao.delete(category.getId());
                    if (deleted) {
                        ToastUtils.showToast(this, "Đã xóa danh mục");
                        loadCategories();
                        setResult(RESULT_OK, new Intent().putExtra("refresh_needed", true));
                    } else {
                        ToastUtils.showToast(this, "Lỗi khi xóa danh mục");
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadCategories() {
        try {
            List<HabitType> categories = habitTypeDao.getAll();
            Log.d(TAG, "Loaded " + categories.size() + " categories");

            // Cập nhật tổng số danh mục
            updateTotalCategories(categories.size());

            for (HabitType category : categories) {
                List<Habit> habits = habitDao.getByType(category.getId());
                category.setHabitCount(habits.size());
                Log.d(TAG, "Category " + category.getName() + " has " + habits.size() + " habits");
            }

            adapter.updateData(categories);
            updateEmptyMessage();
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories: " + e.getMessage());
            ToastUtils.showToast(this, "Lỗi khi tải danh sách danh mục");
        }
    }

    private void updateTotalCategories(int count) {
        if (tvTotalCategories != null) {
            if (count == 0) {
                tvTotalCategories.setVisibility(View.GONE);
            } else {
                tvTotalCategories.setVisibility(View.VISIBLE);
                String text = "Tổng: " + count + " danh mục";
                tvTotalCategories.setText(text);
            }
        }
    }

    private void updateEmptyMessage() {
        if (adapter.getItemCount() == 0) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
            tvTotalCategories.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
            tvTotalCategories.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MainActivity.RequestCode.ADD_CATEGORY) {
                Log.d(TAG, "Category added/updated, reloading data");
                loadCategories();
                setResult(RESULT_OK, new Intent().putExtra("refresh_needed", true));
            } else if (requestCode == REQUEST_CODE_HABIT_DETAIL) {
                if (data != null && data.getBooleanExtra("refresh_needed", false)) {
                    Log.d(TAG, "Habit deleted/updated, reloading data");
                    loadCategories();
                    setResult(RESULT_OK, new Intent().putExtra("refresh_needed", true));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();
    }
}
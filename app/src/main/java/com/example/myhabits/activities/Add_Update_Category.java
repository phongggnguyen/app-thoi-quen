package com.example.myhabits.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myhabits.R;
import com.example.myhabits.adapters.ColorAdapter;
import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.utils.ToastUtils;
import android.os.Handler;
import java.util.Arrays;
import java.util.List;

public class Add_Update_Category extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText edtCategoryName;
    private RecyclerView rvColors;
    private Button btnSave;
    private ImageButton btnMoreColors;
    private ColorAdapter colorAdapter;
    private HabitTypeDao habitTypeDao;
    private String selectedColor;
    private long categoryId = -1;

    private static final List<String> COLORS = Arrays.asList(
            "#FF5252", "#FF4081", "#E040FB", "#7C4DFF",
            "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
            "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41",
            "#FFD740", "#FFAB40", "#FF6E40"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        initViews();
        setupToolbar();
        setupColorRecyclerView();
        setupListeners();
        loadCategoryData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        edtCategoryName = findViewById(R.id.edt_category_name);
        rvColors = findViewById(R.id.rv_colors);
        btnSave = findViewById(R.id.btn_save);
        btnMoreColors = findViewById(R.id.btn_more_colors);
        habitTypeDao = DBManager.getInstance(this).getHabitTypeDao();
        categoryId = getIntent().getLongExtra("category_id", -1);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryId == -1 ?
                    "Thêm danh mục" : "Cập nhật danh mục");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCategoryData() {
        if (categoryId != -1) {
            HabitType category = habitTypeDao.getById(categoryId);
            if (category != null) {
                edtCategoryName.setText(category.getName());
                selectedColor = category.getColor();
                if (colorAdapter != null && selectedColor != null) {
                    colorAdapter.setSelectedColor(selectedColor);
                }
            }
        }
    }
    private void setupColorRecyclerView() {
        colorAdapter = new ColorAdapter(COLORS);
        colorAdapter.setOnColorSelectedListener(color -> selectedColor = color);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        rvColors.setLayoutManager(layoutManager);
        rvColors.setAdapter(colorAdapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveCategory();
            }
        });

        btnMoreColors.setOnClickListener(v -> {
            colorAdapter.toggleExpand();
            float rotation = colorAdapter.isExpanded() ? 180f : 0f;
            btnMoreColors.animate().rotation(rotation).setDuration(200).start();
        });
    }

    private boolean validateInput() {
        String name = edtCategoryName.getText().toString().trim();
        if (name.isEmpty()) {
            edtCategoryName.setError("Vui lòng nhập tên danh mục");
            return false;
        }
        if (selectedColor == null) {
            ToastUtils.showToast(this, "Vui lòng chọn màu sắc");
            return false;
        }
        return true;
    }

    private void saveCategory() {
        if (!validateInput()) return;

        HabitType type = new HabitType();
        type.setName(edtCategoryName.getText().toString().trim());
        type.setColor(selectedColor);

        if (categoryId != -1) {
            // Cập nhật
            type.setId(categoryId);
            int result = habitTypeDao.update(type);
            handleResult(result, "Cập nhật");
        } else {
            // Thêm mới
            long result = habitTypeDao.insert(type);
            if (result == -2) {
                edtCategoryName.setError("Tên danh mục đã tồn tại");
                return;
            }
            handleResult(result > 0 ? 1 : 0, "Thêm");
        }
    }
    private void handleResult(int result, String action) {
        if (result > 0) {
            ToastUtils.showToastAndFinish(this,
                    action + " danh mục thành công!",
                    () -> {
                        setResult(RESULT_OK);
                        finish();
                    });
        } else {
            ToastUtils.showToast(this, "Có lỗi xảy ra, vui lòng thử lại");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
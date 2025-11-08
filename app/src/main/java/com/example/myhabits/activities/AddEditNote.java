package com.example.myhabits.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myhabits.R;
import com.example.myhabits.database.dao.NoteDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Note;
import com.example.myhabits.utils.ToastUtils;

public class AddEditNote extends AppCompatActivity {
    private EditText etTitle, etContent;
    private Button btnSave;
    private NoteDao noteDao;
    private UserDao userDao;
    private Note note;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        initViews();
        setupToolbar();
        loadData();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_note_title);
        etContent = findViewById(R.id.et_note_content);
        btnSave = findViewById(R.id.btn_save_note);

        DBManager dbManager = DBManager.getInstance(this);
        noteDao = dbManager.getNoteDao();
        userDao = dbManager.getUserDao();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEdit ? "Sửa ghi chú" : "Thêm ghi chú");
        }
    }

    private void loadData() {
        long noteId = getIntent().getLongExtra("note_id", -1);
        if (noteId != -1) {
            isEdit = true;
            note = noteDao.getById(noteId);
            if (note != null) {
                etTitle.setText(note.getTitle());
                etContent.setText(note.getContent());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Sửa ghi chú");
                }
            }
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etContent.setError("Vui lòng nhập nội dung");
            return;
        }

        try {
            if (isEdit && note != null) {
                // Cập nhật ghi chú
                note.setTitle(title);
                note.setContent(content);
                note.setUpdatedAt(System.currentTimeMillis());
                noteDao.update(note);
                ToastUtils.showToast(this, "Đã cập nhật ghi chú");
            } else {
                // Thêm ghi chú mới
                Note newNote = new Note(
                        getCurrentUserId(),
                        title,
                        content
                );
                long id = noteDao.insert(newNote);
                if (id > 0) {
                    ToastUtils.showToast(this, "Đã thêm ghi chú");
                } else {
                    ToastUtils.showToast(this, "Không thể thêm ghi chú");
                    return;
                }
            }
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.showToast(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private long getCurrentUserId() {
        return userDao.getFirstUser().getId();
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
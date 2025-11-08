package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.NoteAdapter;
import com.example.myhabits.database.dao.NoteDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Note;
import com.example.myhabits.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ListNote extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {
    private static final int REQUEST_ADD_NOTE = 1;
    private static final int REQUEST_EDIT_NOTE = 2;

    private RecyclerView rvNotes;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private NoteAdapter adapter;
    private List<Note> noteList;
    private NoteDao noteDao;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_note);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadNotes();
        setupListeners();
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rvNotes);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);

        DBManager dbManager = DBManager.getInstance(this);
        noteDao = dbManager.getNoteDao();
        userDao = dbManager.getUserDao();
        noteList = new ArrayList<>();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách ghi chú");
        }
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(this, noteList, this);

        adapter.setOnMoreClickListener(new NoteAdapter.OnMoreClickListener() {
            @Override
            public void onDelete(Note note) {
                new AlertDialog.Builder(ListNote.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa ghi chú này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (noteDao.delete(note.getId())) {
                                loadNotes();
                                ToastUtils.showToast(ListNote.this, "Đã xóa ghi chú");
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(adapter);
    }

    private void loadNotes() {
        noteList.clear();
        noteList.addAll(noteDao.getByUserId(getCurrentUserId()));
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditNote.class);
            startActivityForResult(intent, REQUEST_ADD_NOTE);
        });
    }

    private void updateEmptyView() {
        if (noteList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvNotes.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvNotes.setVisibility(View.VISIBLE);
        }
    }

    private long getCurrentUserId() {
        return userDao.getFirstUser().getId();
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(this, AddEditNote.class);
        intent.putExtra("note_id", note.getId());
        startActivityForResult(intent, REQUEST_EDIT_NOTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && (requestCode == REQUEST_ADD_NOTE || requestCode == REQUEST_EDIT_NOTE)) {
            loadNotes();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list_menu, menu);
        return true;
    }

    private void showDeleteAllConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa tất cả ghi chú?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    noteDao.deleteByUserId(getCurrentUserId());
                    loadNotes();
                    ToastUtils.showToast(this, "Đã xóa tất cả ghi chú");
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete_all) {
            showDeleteAllConfirmDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
package com.example.myhabits.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.models.Habit;
import com.example.myhabits.utils.ToastUtils;

import java.util.List;
@SuppressLint("MissingInflatedId")
public class SearchDialog {
    public static void showSearchDialog(Context context, List<Habit> habits, RecyclerView recyclerView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_search, null);
        EditText searchInput = view.findViewById(R.id.etSearch);

        // Tạo TextView cho title với kích thước tùy chỉnh
        TextView titleView = new TextView(context);
        titleView.setText("Tìm kiếm thói quen");
        titleView.setTextSize(16); // 16sp
        titleView.setPadding(30, 30, 30, 30);
        titleView.setTextColor(context.getColor(R.color.black));

        builder.setCustomTitle(titleView)
                .setView(view)
                .setPositiveButton("Tìm", (dialog, which) -> {
                    String query = searchInput.getText().toString().trim().toLowerCase();
                    boolean found = false;

                    for (int i = 0; i < habits.size(); i++) {
                        if (habits.get(i).getName().toLowerCase().contains(query)) {
                            recyclerView.smoothScrollToPosition(i);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        ToastUtils.showToast(context,
                                "Không tìm thấy thói quen '" + query + "' trong hôm nay");
                    }
                })
                .setNegativeButton("Hủy", null);

        builder.create().show();
    }
}
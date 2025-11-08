package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.Context;

public class GuideDialog {
    public static void showBackupGuide(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Hướng dẫn sao lưu")
                .setMessage(
                        "1. Sao lưu sẽ tạo bản sao của tất cả dữ liệu\n\n" +
                                "2. File sao lưu được lưu tại thư mục:\n" +
                                "   Download/MyHabits\n\n" +
                                "3. Định dạng file: myhabits_backup_YYYYMMDD_HHMMSS.db"
                )
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    public static void showRestoreGuide(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Hướng dẫn khôi phục")
                .setMessage(
                        "1. Chọn file .db đã sao lưu từ thư mục Download/MyHabits\n\n" +
                                "2. Dữ liệu hiện tại sẽ được thay thế bởi dữ liệu từ file sao lưu\n\n" +
                                "3. Sau khi khôi phục thành công, ứng dụng sẽ tự động thoát ra để áp dụng thay đổi\n\n" +
                                "4. Bạn cần mở lại ứng dụng để sử dụng dữ liệu mới"
                )
                .setPositiveButton("Đã hiểu", null)
                .show();
    }
}
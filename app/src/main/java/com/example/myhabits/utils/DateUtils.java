package com.example.myhabits.utils;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    public static Date parseDate(String dateStr) {
        try {
            if(dateStr == null || dateStr.isEmpty()) {
                return null;
            }
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getCurrentDate() {

        return DATE_FORMAT.format(new Date());
    }

    public static String formatDate(Date date) {
        if(date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static boolean isDatePassed(String date) {
        try {
            Date checkDate = DATE_FORMAT.parse(date);
            Date today = truncateTime(Calendar.getInstance().getTime());
            return checkDate != null && checkDate.before(today);
        } catch (ParseException e) {
            Log.e(TAG, "Error checking date passed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }
    public static Date truncateTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static boolean isSameDay(String date1, String date2) {
        try {
            Date d1 = DATE_FORMAT.parse(date1);
            Date d2 = DATE_FORMAT.parse(date2);
            if (d1 == null || d2 == null) return false;

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(d1);
            cal2.setTime(d2);

            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            Log.e(TAG, "Error checking same day: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static int getDayNumberFromStartDate(String startDate, String checkDate) {
        try {
            Date start = DATE_FORMAT.parse(startDate);
            Date check = DATE_FORMAT.parse(checkDate);

            // Chuyển về đầu ngày để so sánh chính xác
            start = truncateTime(start);
            check = truncateTime(check);

            long diffInMillies = check.getTime() - start.getTime();
            long daysBetween = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            return (int) daysBetween + 1; // +1 vì ngày đầu tiên là ngày 1
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static List<String> getDailyDaysWithTarget(String startDate, int targetDays) {
        List<String> days = new ArrayList<>();
        try {
            Date start = DATE_FORMAT.parse(startDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);

            // Thêm ngày bắt đầu
            days.add(DATE_FORMAT.format(cal.getTime()));

            // Thêm các ngày tiếp theo cho đến khi đủ số ngày mục tiêu
            for (int i = 1; i < targetDays; i++) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                days.add(DATE_FORMAT.format(cal.getTime()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }
}
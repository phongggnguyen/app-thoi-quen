package com.example.myhabits.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyReportListAdapter extends RecyclerView.Adapter<DailyReportListAdapter.ViewHolder> {
    private Context context;
    private List<String> dates;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String date);
    }

    public DailyReportListAdapter(Context context) {
        this.context = context;
        this.dates = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = dates.get(position);
        try {
            Date parsedDate = DateUtils.parseDate(date);
            holder.tvDate.setText("Báo cáo ngày " + displayFormat.format(parsedDate));
        } catch (Exception e) {
            holder.tvDate.setText("Báo cáo ngày " + date);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
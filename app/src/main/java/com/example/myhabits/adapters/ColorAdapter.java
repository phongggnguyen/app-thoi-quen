package com.example.myhabits.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myhabits.R;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {
    private List<String> colors;
    private boolean isExpanded = false;
    private int selectedPosition = -1;
    private OnColorSelectedListener listener;
    private static final int COLORS_PER_ROW = 5;

    public ColorAdapter(List<String> colors) {
        this.colors = colors;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colors.get(position);
        holder.viewColor.setBackgroundColor(Color.parseColor(color));
        holder.imgSelected.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onColorSelected(color);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (isExpanded) {
            return colors.size();
        } else {
            return Math.min(COLORS_PER_ROW, colors.size());
        }
    }

    public void toggleExpand() {
        isExpanded = !isExpanded;
        notifyDataSetChanged();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        ImageView imgSelected;

        ColorViewHolder(View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.view_color);
            imgSelected = itemView.findViewById(R.id.img_selected);
        }
    }

    public void setSelectedColor(String color) {
        // Tìm vị trí của màu trong danh sách
        selectedPosition = colors.indexOf(color);
        notifyDataSetChanged();
    }

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }
}
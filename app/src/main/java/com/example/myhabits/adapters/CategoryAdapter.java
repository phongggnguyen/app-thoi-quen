package com.example.myhabits.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.models.HabitType;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<HabitType> categories;
    private OnCategoryClickListener listener;


    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_menu, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        HabitType category = categories.get(position);
        holder.tvName.setText(category.getName());

        GradientDrawable drawable = (GradientDrawable) holder.viewColor.getBackground();
        drawable.setColor(Color.parseColor(category.getColor()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        TextView tvName;

        CategoryViewHolder(View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.view_color);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(HabitType category);
    }
}
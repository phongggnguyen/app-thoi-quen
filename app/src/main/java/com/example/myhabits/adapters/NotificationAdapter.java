package com.example.myhabits.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.models.HabitNotification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private List<HabitNotification> notifications;
    private OnNotificationClickListener clickListener;
    private OnMoreClickListener moreClickListener;
    private SimpleDateFormat timeFormat;

    public interface OnNotificationClickListener {
        void onNotificationClick(HabitNotification notification);
    }

    public interface OnMoreClickListener {
        void onDelete(HabitNotification notification);
    }

    public NotificationAdapter(Context context) {
        this.context = context;
        this.notifications = new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        this.moreClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitNotification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<HabitNotification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        ImageButton btnMore;

        ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvContent = view.findViewById(R.id.tvContent);
            tvTime = view.findViewById(R.id.tvTime);
            btnMore = view.findViewById(R.id.btnMore);

            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onNotificationClick(notifications.get(position));
                }
            });

            btnMore.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(v, notifications.get(position));
                }
            });
        }

        public void bind(HabitNotification notification) {
            tvTitle.setText(notification.getTitle());
            tvContent.setText(notification.getContent());
            tvTime.setText(timeFormat.format(notification.getNotifyTime()));

            itemView.setBackgroundColor(notification.isRead() ?
                    context.getColor(R.color.white) :
                    context.getColor(R.color.unread_notification));
        }

        private void showPopupMenu(View view, HabitNotification notification) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.item_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_delete) {
                    if (moreClickListener != null) {
                        moreClickListener.onDelete(notification);
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        }
    }
}
package com.example.myhabits.utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;

public class SelectAvatarDialog extends DialogFragment {
    private int[] avatarResources;
    private OnAvatarSelectedListener listener;

    public interface OnAvatarSelectedListener {
        void onAvatarSelected(int resourceId);
    }

    public static SelectAvatarDialog newInstance() {
        return new SelectAvatarDialog();
    }
    private static final int[] AVATAR_RESOURCES = {
            R.drawable.ic_profile,
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_avatar, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAvatars);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        AvatarAdapter adapter = new AvatarAdapter(AVATAR_RESOURCES, resourceId -> {
            if (listener != null) {
                listener.onAvatarSelected(resourceId);
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void setAvatarResources(int[] resources) {
        this.avatarResources = resources;
    }

    public void setOnAvatarSelectedListener(OnAvatarSelectedListener listener) {
        this.listener = listener;
    }

    private static class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {
        private final int[] avatarResources;
        private final OnAvatarClickListener listener;

        interface OnAvatarClickListener {
            void onAvatarClick(int resourceId);
        }

        AvatarAdapter(int[] avatarResources, OnAvatarClickListener listener) {
            this.avatarResources = avatarResources;
            this.listener = listener;
        }

        @NonNull
        @Override
        public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_avatar, parent, false);
            return new AvatarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
            int resourceId = avatarResources[position];
            holder.imageView.setImageResource(resourceId);
            holder.itemView.setOnClickListener(v -> listener.onAvatarClick(resourceId));
        }

        @Override
        public int getItemCount() {
            return avatarResources.length;
        }

        static class AvatarViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            AvatarViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageViewAvatar);
            }
        }
    }
}
package com.sonichub.app.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemNotificationBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    public interface Listener {
        void onNotification(Models.Notification item);
    }

    private final List<Models.Notification> items = new ArrayList<>();
    private final Listener listener;

    public NotificationAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Models.Notification> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Notification item = items.get(position);
        holder.binding.avatar.setCover(item.actorAvatar != null && !item.actorAvatar.isEmpty()
                ? item.actorAvatar : "c2");
        holder.binding.avatar.setCircle(true);
        holder.binding.title.setText(item.title);
        holder.binding.body.setText(item.body);
        holder.binding.time.setText(Ui.relativeTime(item.createdAt));
        holder.binding.unreadDot.setVisibility(item.unread() ? View.VISIBLE : View.INVISIBLE);
        holder.binding.getRoot().setOnClickListener(v -> listener.onNotification(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;

        VH(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

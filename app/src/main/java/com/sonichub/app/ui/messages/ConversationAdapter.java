package com.sonichub.app.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemConversationBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {
    public interface Listener {
        void onConversation(Models.Conversation item);
    }

    private final List<Models.Conversation> items = new ArrayList<>();
    private final Listener listener;

    public ConversationAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Models.Conversation> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemConversationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Conversation item = items.get(position);
        holder.binding.avatar.setCover(item.peerAvatar);
        holder.binding.avatar.setCircle(true);
        holder.binding.name.setText(item.peerName);
        holder.binding.lastMessage.setText(item.lastMessage);
        holder.binding.time.setText(Ui.relativeTime(item.updatedAt));
        holder.binding.unread.setVisibility(item.unread > 0 ? View.VISIBLE : View.GONE);
        holder.binding.unread.setText(String.valueOf(item.unread));
        holder.binding.getRoot().setOnClickListener(v -> listener.onConversation(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemConversationBinding binding;

        VH(ItemConversationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

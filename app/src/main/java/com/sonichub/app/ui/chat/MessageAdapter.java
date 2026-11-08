package com.sonichub.app.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.R;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {
    private final List<Models.Message> items = new ArrayList<>();

    public void setItems(List<Models.Message> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void append(Models.Message item) {
        if (item == null) return;
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Message item = items.get(position);
        boolean self = item.senderId != null && item.senderId.equals(Session.userId());
        holder.binding.row.setGravity(self ? Gravity.END : Gravity.START);
        holder.binding.bubble.setBackgroundResource(self ? R.drawable.bg_chat_self : R.drawable.bg_chat_other);
        holder.binding.bubble.setTextColor(holder.itemView.getContext().getColor(
                self ? R.color.bg : R.color.text));

        String text = item.text;
        if ("song".equals(item.type) && item.work != null) {
            text = "[歌曲] " + item.work.title + "\n" + item.work.ownerName;
        }
        holder.binding.bubble.setText(text);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemMessageBinding binding;

        VH(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

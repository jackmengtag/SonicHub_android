package com.sonichub.app.ui.player;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemCommentBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {
    private final List<Models.Comment> items = new ArrayList<>();

    public void setItems(List<Models.Comment> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void append(Models.Comment item) {
        if (item == null) return;
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Comment item = items.get(position);
        holder.binding.avatar.setCover(item.avatar != null ? item.avatar : "c3");
        holder.binding.avatar.setCircle(true);
        holder.binding.name.setText(item.name);
        holder.binding.content.setText(item.content);
        holder.binding.time.setText(Ui.relativeTime(item.createdAt));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemCommentBinding binding;

        VH(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

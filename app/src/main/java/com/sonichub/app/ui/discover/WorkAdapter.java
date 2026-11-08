package com.sonichub.app.ui.discover;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemSongBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.VH> {
    public interface Listener {
        void onWork(Models.Work work);

        void onLike(Models.Work work);
    }

    private final List<Models.Work> items = new ArrayList<>();
    private final Listener listener;

    public WorkAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Models.Work> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void update(Models.Work work) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(work.id)) {
                items.set(i, work);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemSongBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Work work = items.get(position);
        holder.binding.cover.setCover(work.cover);
        holder.binding.title.setText(work.title);
        holder.binding.subtitle.setText(work.ownerName + " · " + work.tagsText());
        holder.binding.tags.setText(work.tagsText() + (work.isAi ? " · AI 生成" : ""));
        holder.binding.aiBadge.setVisibility(work.isAi ? android.view.View.VISIBLE : android.view.View.GONE);
        holder.binding.duration.setText(work.durationText());
        holder.binding.likeCount.setText(Ui.count(work.likeCount));

        int color = ContextCompat.getColor(holder.itemView.getContext(),
                work.liked ? R.color.accent : R.color.text_3);
        holder.binding.likeIcon.setImageTintList(ColorStateList.valueOf(color));
        holder.binding.likeCount.setTextColor(color);

        holder.binding.getRoot().setOnClickListener(v -> listener.onWork(work));
        holder.binding.likeButton.setOnClickListener(v -> listener.onLike(work));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemSongBinding binding;

        VH(ItemSongBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

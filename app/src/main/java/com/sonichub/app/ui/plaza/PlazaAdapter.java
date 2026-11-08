package com.sonichub.app.ui.plaza;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemPlazaBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class PlazaAdapter extends RecyclerView.Adapter<PlazaAdapter.VH> {
    public interface Listener {
        void onWork(Models.Work work);
    }

    private final List<Models.Work> items = new ArrayList<>();
    private final Listener listener;

    public PlazaAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Models.Work> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemPlazaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Work work = items.get(position);
        holder.binding.cover.setCover(work.cover);
        holder.binding.title.setText(work.title);
        holder.binding.meta.setText(work.ownerName + " · " + Ui.count(work.playCount) + " 播放");
        String tag = work.genre != null && !work.genre.isEmpty() ? work.genre : "AI 音乐";
        holder.binding.tag.setText(tag);
        holder.binding.getRoot().setOnClickListener(v -> listener.onWork(work));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPlazaBinding binding;

        VH(ItemPlazaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

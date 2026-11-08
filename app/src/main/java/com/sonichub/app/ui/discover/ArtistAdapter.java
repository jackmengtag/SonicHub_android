package com.sonichub.app.ui.discover;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemArtistBinding;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.VH> {
    public interface Listener {
        void onArtist(Models.UserCard artist);
    }

    private final List<Models.UserCard> items = new ArrayList<>();
    private final Listener listener;

    public ArtistAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Models.UserCard> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemArtistBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.UserCard card = items.get(position);
        holder.binding.cover.setCover(card.avatar);
        holder.binding.cover.setCircle(true);
        holder.binding.name.setText(card.name);
        holder.binding.genres.setText(card.stageName != null && !card.stageName.isEmpty()
                ? card.stageName : "音乐人");
        holder.binding.followButton.setText("查看");
        holder.binding.getRoot().setOnClickListener(v -> listener.onArtist(card));
        holder.binding.followButton.setOnClickListener(v -> listener.onArtist(card));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemArtistBinding binding;

        VH(ItemArtistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

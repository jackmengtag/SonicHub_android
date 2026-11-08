package com.sonichub.app.ui.credits;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemPackageBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.VH> {
    public interface Listener {
        void onPackage(Models.PackageItem item);
    }

    private final List<Models.PackageItem> items = new ArrayList<>();
    private final Listener listener;
    private String selectedId;

    public PackageAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Models.PackageItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        if (selectedId == null && !items.isEmpty()) {
            selectedId = items.get(0).id;
        }
        notifyDataSetChanged();
    }

    public void select(String id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public Models.PackageItem selected() {
        for (Models.PackageItem item : items) {
            if (item.id.equals(selectedId)) return item;
        }
        return items.isEmpty() ? null : items.get(0);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemPackageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.PackageItem item = items.get(position);
        holder.binding.credits.setText(String.valueOf(item.credits));
        holder.binding.price.setText(String.format(Locale.CHINA, "¥ %.2f", item.priceCents / 100f));
        holder.binding.tag.setText(item.tag);
        holder.binding.getRoot().setBackgroundResource(item.id.equals(selectedId)
                ? R.drawable.bg_card_selected : R.drawable.bg_card);
        holder.binding.getRoot().setOnClickListener(v -> {
            selectedId = item.id;
            notifyDataSetChanged();
            listener.onPackage(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPackageBinding binding;

        VH(ItemPackageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

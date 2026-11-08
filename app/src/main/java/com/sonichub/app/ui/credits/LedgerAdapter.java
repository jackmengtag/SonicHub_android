package com.sonichub.app.ui.credits;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemLedgerBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class LedgerAdapter extends RecyclerView.Adapter<LedgerAdapter.VH> {
    private final List<Models.LedgerEntry> items = new ArrayList<>();

    public void setItems(List<Models.LedgerEntry> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemLedgerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.LedgerEntry item = items.get(position);
        holder.binding.type.setText(typeLabel(item.entryType));
        holder.binding.ref.setText((item.refId == null ? "" : item.refId) + " · "
                + Ui.relativeTime(item.createdAt));
        String amount = (item.amount > 0 ? "+" : "") + item.amount;
        holder.binding.amount.setText(amount);
        holder.binding.amount.setTextColor(holder.itemView.getContext().getColor(
                item.amount >= 0 ? R.color.mint : R.color.text));
    }

    private String typeLabel(String type) {
        if (type == null) return "积分变动";
        switch (type) {
            case "reserve":
                return "生成冻结";
            case "capture":
                return "生成扣减";
            case "release":
                return "失败退还";
            case "recharge":
                return "充值到账";
            case "grant":
                return "新人赠送";
            default:
                return type;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemLedgerBinding binding;

        VH(ItemLedgerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

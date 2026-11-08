package com.sonichub.app.ui.works;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ItemWorkBinding;

import java.util.ArrayList;
import java.util.List;

public class WorkManageAdapter extends RecyclerView.Adapter<WorkManageAdapter.VH> {
    public interface Listener {
        void onWork(Models.Work work);
    }

    private final List<Models.Work> items = new ArrayList<>();
    private final Listener listener;

    public WorkManageAdapter(Listener listener) {
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
        return new VH(ItemWorkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Models.Work item = items.get(position);
        holder.binding.cover.setCover(item.cover);
        holder.binding.title.setText(item.title);
        holder.binding.meta.setText(visibilityLabel(item.visibility) + " · " + item.playCount + " 播放 · "
                + item.likeCount + " 赞");
        String status = item.status == null ? "draft" : item.status;
        int bg;
        int color;
        String label;
        switch (status) {
            case "published":
                bg = R.drawable.bg_pill_green;
                color = R.color.mint;
                label = "已发布";
                break;
            case "pending_review":
                bg = R.drawable.bg_pill_orange;
                color = R.color.gold;
                label = "审核中";
                break;
            case "rejected":
            case "removed":
                bg = R.drawable.bg_pill_red;
                color = R.color.danger;
                label = "rejected".equals(status) ? "未通过" : "已下架";
                break;
            default:
                bg = R.drawable.bg_pill_gray;
                color = R.color.text_3;
                label = "草稿";
                break;
        }
        holder.binding.status.setBackgroundResource(bg);
        holder.binding.status.setTextColor(holder.itemView.getContext().getColor(color));
        holder.binding.status.setText(label);
        boolean showReject = item.rejectReason != null && !item.rejectReason.isEmpty();
        holder.binding.reject.setVisibility(showReject ? View.VISIBLE : View.GONE);
        holder.binding.reject.setText(item.rejectReason);
        holder.binding.getRoot().setOnClickListener(v -> listener.onWork(item));
    }

    private String visibilityLabel(String value) {
        if ("friends".equals(value)) return "朋友";
        if ("private".equals(value)) return "私密";
        return "公开";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemWorkBinding binding;

        VH(ItemWorkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

package com.sonichub.app.ui.works;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityWorksBinding;
import com.sonichub.app.ui.player.PlayerActivity;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class WorksActivity extends AppCompatActivity implements WorksContract.View {
    private ActivityWorksBinding binding;
    private WorksPresenter presenter;
    private WorkManageAdapter adapter;
    private String status;
    private final List<TextView> chips = new ArrayList<>();
    private final String[] labels = {"全部", "已发布", "审核中", "未通过", "草稿"};
    private final String[] values = {"", "published", "pending_review", "rejected", "draft"};
    private boolean resumedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new WorksPresenter(this);
        adapter = new WorkManageAdapter(work -> {
            if ("published".equals(work.status)) {
                Intent intent = new Intent(this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_WORK_ID, work.id);
                intent.putExtra(PlayerActivity.EXTRA_TITLE, work.title);
                startActivity(intent);
            } else {
                Ui.toast(this, "当前状态：" + work.status);
            }
        });
        binding.workList.setLayoutManager(new LinearLayoutManager(this));
        binding.workList.setAdapter(adapter);
        binding.backButton.setOnClickListener(v -> finish());
        buildChips();
        select(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从创作/发布流程返回时重新拉取，保证新作品立即出现在列表里。
        if (resumedOnce) {
            presenter.load(status);
        }
        resumedOnce = true;
    }

    private void buildChips() {
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView chip = new TextView(this);
            chip.setText(labels[i]);
            chip.setTextSize(11.5f);
            chip.setPadding(Ui.dp(this, 14), Ui.dp(this, 7), Ui.dp(this, 14), Ui.dp(this, 7));
            chip.setBackgroundResource(R.drawable.bg_chip);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = Ui.dp(this, 8);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> select(index));
            chips.add(chip);
            binding.filterRow.addView(chip);
        }
    }

    private void select(int index) {
        for (int i = 0; i < chips.size(); i++) {
            TextView chip = chips.get(i);
            boolean selected = i == index;
            chip.setSelected(selected);
            chip.setTextColor(getResources().getColor(selected ? R.color.accent : R.color.text_2));
            chip.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
        status = values[index].isEmpty() ? null : values[index];
        presenter.load(status);
    }

    @Override
    public void showWorks(List<Models.Work> works) {
        adapter.setItems(works);
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
    }
}

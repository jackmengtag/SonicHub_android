package com.sonichub.app.ui.verify;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityVerifyBinding;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class VerifyActivity extends AppCompatActivity implements VerifyContract.View {
    private ActivityVerifyBinding binding;
    private VerifyPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new VerifyPresenter(this);
        binding.backButton.setOnClickListener(v -> finish());
        setupGenres();
        binding.submitButton.setOnClickListener(v -> submit());
        presenter.load();
    }

    private void setupGenres() {
        for (String genre : new String[]{"电子", "Lo-fi", "国风", "摇滚", "流行", "R&B"}) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setChecked("电子".equals(genre));
            chip.setCheckedIconVisible(false);
            chip.setChipCornerRadius(Ui.dp(this, 18));
            chip.setChipMinHeight(Ui.dp(this, 34));
            chip.setTextSize(11.5f);
            int[][] states = {{android.R.attr.state_checked}, {}};
            chip.setChipBackgroundColor(new ColorStateList(states, new int[]{
                    getColor(R.color.accent_soft), getColor(R.color.surface_2)}));
            chip.setChipStrokeColor(new ColorStateList(states, new int[]{
                    getColor(R.color.accent), getColor(R.color.line)}));
            chip.setTextColor(new ColorStateList(states, new int[]{
                    getColor(R.color.accent), getColor(R.color.text_2)}));
            binding.genreGroup.addView(chip);
        }
    }

    private void submit() {
        String stage = binding.stageInput.getText().toString().trim();
        String bio = binding.bioInput.getText().toString().trim();
        List<String> genres = new ArrayList<>();
        for (int id : binding.genreGroup.getCheckedChipIds()) {
            Chip chip = binding.genreGroup.findViewById(id);
            if (chip != null) genres.add(chip.getText().toString());
        }
        if (stage.length() < 2 || stage.length() > 20) {
            Ui.toast(this, "艺名长度需为 2–20 字");
            return;
        }
        if (genres.isEmpty()) {
            Ui.toast(this, "请至少选择 1 个擅长风格");
            return;
        }
        binding.submitButton.setEnabled(false);
        binding.submitButton.setText("提交中…");
        presenter.apply(stage, bio, genres);
    }

    @Override
    public void showStatus(Models.Musician musician) {
        if (musician == null) {
            binding.statusText.setText("未申请");
            binding.statusText.setTextColor(getColor(R.color.text_3));
            binding.statusText.setBackgroundResource(R.drawable.bg_pill_gray);
            return;
        }
        binding.stageInput.setText(musician.stageName);
        binding.bioInput.setText(musician.bio);
        String status = musician.status == null ? "" : musician.status;
        binding.statusText.setText("pending".equals(status) ? "审核中"
                : "verified".equals(status) ? "已认证"
                : "rejected".equals(status) ? "未通过" : status);
        binding.statusText.setTextColor(getColor("verified".equals(status) ? R.color.mint
                : "rejected".equals(status) ? R.color.danger : R.color.gold));
        binding.statusText.setBackgroundResource("verified".equals(status)
                ? R.drawable.bg_pill_green
                : "rejected".equals(status) ? R.drawable.bg_pill_red : R.drawable.bg_pill_orange);
        boolean locked = "pending".equals(status) || "verified".equals(status);
        binding.submitButton.setEnabled(!locked);
        binding.submitButton.setText(locked ? "审核中，请耐心等待" : "提交认证申请");
    }

    @Override
    public void onApplied() {
        Ui.toast(this, "认证申请已提交");
        binding.statusText.setText("审核中");
        binding.statusText.setTextColor(getColor(R.color.gold));
        binding.statusText.setBackgroundResource(R.drawable.bg_pill_orange);
        binding.submitButton.setEnabled(false);
        binding.submitButton.setText("审核中，请耐心等待");
    }

    @Override
    public void showError(String message) {
        binding.submitButton.setEnabled(true);
        binding.submitButton.setText("提交认证申请");
        Ui.toast(this, message);
    }
}

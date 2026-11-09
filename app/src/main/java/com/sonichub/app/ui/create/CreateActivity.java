package com.sonichub.app.ui.create;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sonichub.app.R;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityCreateBinding;
import com.sonichub.app.ui.login.LoginActivity;
import com.sonichub.app.ui.music.MusicCreateActivity;
import com.sonichub.app.ui.player.PlayerActivity;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class CreateActivity extends AppCompatActivity implements CreateContract.View {
    private ActivityCreateBinding binding;
    private CreatePresenter presenter;
    private int step = 1;
    private String quoteId;
    private String generationId;
    private String workId;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (generationId != null) {
                presenter.poll(generationId);
                handler.postDelayed(this, 1500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.isLoggedIn()) {
            Ui.toast(this, "请先登录后写歌");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        binding = ActivityCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new CreatePresenter(this);

        setupTemplates();
        setupChips();
        setStep(1);

        binding.closeButton.setOnClickListener(v -> finish());
        binding.backStep.setOnClickListener(v -> {
            if (step > 1) setStep(step - 1);
            else finish();
        });
        binding.nextButton.setOnClickListener(v -> onNext());
        // 入口：跳到直接对接本地音乐大模型的「AI 写歌」页
        binding.aiMusicEntry.setOnClickListener(v ->
                startActivity(new Intent(this, MusicCreateActivity.class)));
        binding.cancelButton.setOnClickListener(v -> {
            if (generationId != null) presenter.cancel(generationId);
        });
        binding.publishButton.setOnClickListener(v -> publish());
    }

    private void setupTemplates() {
        String[][] templates = {
                {"短视频 BGM", "15 秒竖屏视频，开头抓耳，节奏轻快，不要人声"},
                {"生日祝福", "给朋友的生日歌，温暖，木吉他，简单歌词"},
                {"纯音乐", "深夜写作时的 Lo-fi 纯音乐，循环不腻"}
        };
        for (String[] template : templates) {
            TextView chip = new TextView(this);
            chip.setText(template[0]);
            chip.setTextSize(11.5f);
            chip.setTextColor(getResources().getColor(R.color.text_2));
            chip.setBackgroundResource(R.drawable.bg_chip);
            chip.setPadding(Ui.dp(this, 14), Ui.dp(this, 7), Ui.dp(this, 14), Ui.dp(this, 7));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = Ui.dp(this, 8);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> binding.promptInput.setText(template[1]));
            binding.templateRow.addView(chip);
        }
    }

    private void setupChips() {
        String[] styles = {"流行", "电子", "Lo-fi", "国风", "摇滚", "R&B", "影视配乐", "爵士"};
        for (String style : styles) {
            Chip chip = makeChip(style, "电子".equals(style));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && binding.styleGroup.getCheckedChipIds().size() > 3) {
                    buttonView.setChecked(false);
                    Ui.toast(this, "最多选择 3 个风格");
                }
            });
            binding.styleGroup.addView(chip);
        }
        for (String value : new String[]{"15 秒", "30 秒", "60 秒", "90 秒", "120 秒"}) {
            binding.durationGroup.addView(makeChip(value, "30 秒".equals(value)));
        }
        for (String value : new String[]{"女声", "男声", "纯音乐"}) {
            binding.vocalGroup.addView(makeChip(value, "女声".equals(value)));
        }
        for (String value : new String[]{"标准", "高清"}) {
            binding.qualityGroup.addView(makeChip(value, "高清".equals(value)));
        }
        for (String value : new String[]{"公开", "朋友", "私密"}) {
            binding.visibilityGroup.addView(makeChip(value, "公开".equals(value)));
        }
    }

    private Chip makeChip(String label, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setCheckedIconVisible(false);
        chip.setChipCornerRadius(Ui.dp(this, 18));
        chip.setChipMinHeight(Ui.dp(this, 34));
        chip.setTextSize(11.5f);
        int[][] states = {{android.R.attr.state_checked}, {}};
        chip.setChipBackgroundColor(new ColorStateList(states, new int[]{
                getResources().getColor(R.color.accent_soft),
                getResources().getColor(R.color.surface_2)
        }));
        chip.setChipStrokeColor(new ColorStateList(states, new int[]{
                getResources().getColor(R.color.accent),
                getResources().getColor(R.color.line)
        }));
        chip.setTextColor(new ColorStateList(states, new int[]{
                getResources().getColor(R.color.accent),
                getResources().getColor(R.color.text_2)
        }));
        chip.setChipStrokeWidth(Ui.dp(this, 1));
        return chip;
    }

    private void onNext() {
        if (step == 1) {
            if (TextUtils.isEmpty(binding.promptInput.getText().toString().trim())) {
                Ui.toast(this, "请先描述你想写的歌");
                return;
            }
            setStep(2);
        } else if (step == 2) {
            List<String> styles = selectedLabels(binding.styleGroup);
            if (styles.isEmpty()) {
                Ui.toast(this, "请至少选择 1 个风格");
                return;
            }
            String quality = selectedOne(binding.qualityGroup, "标准");
            binding.nextButton.setEnabled(false);
            binding.nextButton.setText("获取报价…");
            presenter.quote(styles, duration(), quality, binding.stemsSwitch.isChecked());
        } else if (step == 3) {
            if (!binding.agree1.isChecked() || !binding.agree2.isChecked()) {
                Ui.toast(this, "请先阅读并同意相关说明");
                return;
            }
            binding.nextButton.setEnabled(false);
            binding.nextButton.setText("创建任务…");
            presenter.generate(quoteId, binding.promptInput.getText().toString().trim(),
                    binding.lyricsInput.getText().toString().trim(),
                    selectedLabels(binding.styleGroup), duration(),
                    selectedOne(binding.vocalGroup, "女声"),
                    selectedOne(binding.qualityGroup, "标准"),
                    binding.stemsSwitch.isChecked(),
                    visibilityValue(selectedOne(binding.visibilityGroup, "公开")));
        }
    }

    private void publish() {
        if (!binding.aiCheck.isChecked()) {
            Ui.toast(this, "请勾选 AI 生成声明");
            return;
        }
        if (workId == null) return;
        String title = binding.workTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Ui.toast(this, "请填写作品名");
            return;
        }
        binding.publishButton.setEnabled(false);
        presenter.publish(workId, title, visibilityValue(selectedOne(binding.visibilityGroup, "公开")),
                selectedLabels(binding.styleGroup));
    }

    private String visibilityValue(String label) {
        if ("朋友".equals(label)) return "friends";
        if ("私密".equals(label)) return "private";
        return "public";
    }

    private int duration() {
        String label = selectedOne(binding.durationGroup, "30 秒");
        StringBuilder digits = new StringBuilder();
        for (char c : label.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (Exception e) {
            return 30;
        }
    }

    private List<String> selectedLabels(ChipGroup group) {
        List<String> out = new ArrayList<>();
        for (int id : group.getCheckedChipIds()) {
            View view = group.findViewById(id);
            if (view instanceof Chip) out.add(((Chip) view).getText().toString());
        }
        return out;
    }

    private String selectedOne(ChipGroup group, String fallback) {
        List<String> selected = selectedLabels(group);
        return selected.isEmpty() ? fallback : selected.get(0);
    }

    private void setStep(int value) {
        step = value;
        binding.step1.setVisibility(value == 1 ? View.VISIBLE : View.GONE);
        binding.step2.setVisibility(value == 2 ? View.VISIBLE : View.GONE);
        binding.step3.setVisibility(value == 3 ? View.VISIBLE : View.GONE);
        binding.step4.setVisibility(value == 4 ? View.VISIBLE : View.GONE);
        binding.stepText.setText(value + " / 4");
        binding.stepProgress.setProgress(value * 25);
        binding.backStep.setVisibility(value > 1 && value < 4 ? View.VISIBLE : View.GONE);
        binding.nextButton.setVisibility(value == 4 ? View.GONE : View.VISIBLE);
        binding.nextButton.setEnabled(true);
        if (value == 1) binding.nextButton.setText("下一步 · 风格与参数");
        if (value == 2) binding.nextButton.setText("获取报价");
        if (value == 3) binding.nextButton.setText("确认并开始生成");
    }

    @Override
    public void showQuote(Models.Quote quote, long balance) {
        quoteId = quote.quoteId;
        binding.quoteSummary.setText(String.join(" / ", selectedLabels(binding.styleGroup))
                + " · " + duration() + " 秒 · " + selectedOne(binding.vocalGroup, "女声"));
        StringBuilder detail = new StringBuilder();
        detail.append("基础生成  ").append(quote.base).append(" 积分\n");
        if (quote.qualityFee > 0) detail.append("高清音质  +").append(quote.qualityFee).append("\n");
        if (quote.stemsFee > 0) detail.append("分轨输出  +").append(quote.stemsFee).append("\n");
        detail.append("合计  ").append(quote.total).append(" 积分");
        binding.quoteDetail.setText(detail.toString());
        if (balance >= 0) {
            binding.balanceText.setText("当前余额 " + balance + " 积分 · 生成后 "
                    + Math.max(0, balance - quote.total) + " 积分");
        } else {
            binding.balanceText.setText("本次消耗 " + quote.total + " 积分");
        }
        setStep(3);
    }

    @Override
    public void showGenerating(Models.Generation generation) {
        generationId = generation.id;
        setStep(4);
        binding.resultGroup.setVisibility(View.GONE);
        binding.cancelButton.setVisibility(View.VISIBLE);
        binding.progressHint.setVisibility(View.VISIBLE);
        binding.stageText.setText("任务已创建，正在排队…");
        binding.genProgress.setProgress(5);
        handler.removeCallbacks(poller);
        handler.post(poller);
    }

    @Override
    public void updateGeneration(Models.Generation generation) {
        binding.genProgress.setProgress(generation.progress);
        String stage = generation.stage == null ? "生成中" : generation.stage;
        switch (stage) {
            case "queued":
                stage = "排队中";
                break;
            case "generating":
                stage = "正在生成旋律与和声";
                break;
            case "composing":
                stage = "正在编排";
                break;
            case "rendering":
                stage = "正在渲染音频";
                break;
            case "mixing":
                stage = "正在混音与母带";
                break;
            case "completed":
                stage = "生成完成";
                break;
            default:
                break;
        }
        binding.stageText.setText(stage + " · " + generation.progress + "%");
    }

    @Override
    public void showResult(String workId) {
        this.workId = workId;
        handler.removeCallbacks(poller);
        generationId = null;
        binding.genProgress.setProgress(100);
        binding.stageText.setText("生成完成");
        binding.progressHint.setText("可以试听并发布，也可以保存为私密作品");
        binding.cancelButton.setVisibility(View.GONE);
        binding.resultGroup.setVisibility(View.VISIBLE);
        binding.nextButton.setVisibility(View.GONE);
        if (TextUtils.isEmpty(binding.workTitle.getText())) {
            binding.workTitle.setText("我的 AI 作品");
        }
    }

    @Override
    public void published(Models.Work work) {
        Ui.toast(this, "已提交审核并发布");
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_WORK_ID, work.id);
        intent.putExtra(PlayerActivity.EXTRA_TITLE, work.title);
        startActivity(intent);
        finish();
    }

    @Override
    public void cancelled() {
        handler.removeCallbacks(poller);
        generationId = null;
        Ui.toast(this, "已取消生成，积分已退还");
        finish();
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
        binding.nextButton.setEnabled(true);
        binding.publishButton.setEnabled(true);
        if (step == 2) binding.nextButton.setText("获取报价");
        if (step == 3) binding.nextButton.setText("确认并开始生成");
        if (step == 4 && generationId != null) {
            handler.removeCallbacks(poller);
            generationId = null;
            binding.stageText.setText("生成失败，请重试");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(poller);
    }
}

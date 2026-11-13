package com.sonichub.app.ui.music;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sonichub.app.data.ApiClient;
import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityMusicCreateBinding;
import com.sonichub.app.ui.login.LoginActivity;
import com.sonichub.app.util.Ui;

/**
 * 「AI 写歌」页面：歌名 → AI 写词（可选）→ 生成歌曲 → 试听。
 * <p>
 * 写词与生成音乐都走主后端的手机端接口：
 * POST /api/v1/mobile/lyrics、POST /api/v1/mobile/music，
 * 生成后轮询 GET /api/v1/generations/{id}，成功后读取作品音频。
 */
public class MusicCreateActivity extends AppCompatActivity {
    private static final String[] GENRE_LABELS = {"流行", "民谣", "国风", "摇滚", "电子", "爵士", "R&B", "影视配乐"};
    private static final String[] GENRE_VALUES = {"pop", "folk", "guofeng", "rock", "electronic", "jazz", "r_and_b", "cinematic"};
    private static final String[] MOOD_LABELS = {"浪漫", "伤感", "愉快", "宁静", "治愈", "史诗", "忧郁"};
    private static final String[] MOOD_VALUES = {"romantic", "sad", "happy", "peaceful", "healing", "epic", "melancholy"};
    private static final String[] VOCAL_LABELS = {"自动", "女声", "男声"};
    private static final String[] VOCAL_VALUES = {"auto", "female", "male"};
    private static final String[] DURATION_LABELS = {"15 秒", "30 秒", "60 秒", "3 分钟"};
    private static final int[] DURATION_VALUES = {15, 30, 60, 180};
    private static final long POLL_INTERVAL = 2000L;

    private ActivityMusicCreateBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private String taskId;      // 当前生成任务 ID（generation_id）
    private String audioUrl;    // 成品绝对地址

    /** 每 2 秒轮询一次生成进度。 */
    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (taskId == null) return;
            final String id = taskId;
            Repository.get().generation(id, new RepoCallback<Models.Generation>() {
                @Override
                public void onSuccess(Models.Generation gen) {
                    if (!id.equals(taskId)) return;  // 已切换任务，忽略旧回调
                    binding.genProgress.setProgress(Math.max(0, Math.min(100, gen.progress)));
                    binding.stageText.setText(stageText(gen.stage, gen.status));
                    if ("succeeded".equals(gen.status)) {
                        onFinished(gen);
                    } else if ("failed".equals(gen.status)) {
                        fail(TextUtils.isEmpty(gen.error) ? "生成失败，请重试" : gen.error);
                    } else if ("cancelled".equals(gen.status)) {
                        fail("任务已取消");
                    } else {
                        handler.postDelayed(poller, POLL_INTERVAL);
                    }
                }

                @Override
                public void onError(String message) {
                    fail("查询进度失败：" + message);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSpinners();
        setupBpm();
        refreshServerText();

        binding.closeButton.setOnClickListener(v -> finish());
        binding.serverText.setOnClickListener(v -> editServer());
        binding.aiLyricsButton.setOnClickListener(v -> requestLyrics());
        binding.generateButton.setOnClickListener(v -> generate());
        binding.playButton.setOnClickListener(v -> togglePlay());
    }

    // ────────────── 初始化 ──────────────
    private void setupSpinners() {
        bindSpinner(binding.genreSpinner, GENRE_LABELS, 0);
        bindSpinner(binding.moodSpinner, MOOD_LABELS, 0);
        bindSpinner(binding.vocalSpinner, VOCAL_LABELS, 0);
        bindSpinner(binding.durationSpinner, DURATION_LABELS, 1);  // 默认 30 秒
    }

    private void bindSpinner(android.widget.Spinner spinner, String[] labels, int selected) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selected);
    }

    private void setupBpm() {
        binding.bpmSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.bpmValue.setText(bpm() + " BPM");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        binding.bpmValue.setText(bpm() + " BPM");
    }

    private void refreshServerText() {
        String base = Session.baseUrl()
                .replace("http://", "").replace("https://", "");
        binding.serverText.setText("服务：" + base + "（点击修改）");
    }

    /** 修改后端地址（模拟器 10.0.2.2，真机填电脑局域网 IP）。 */
    private void editServer() {
        EditText input = new EditText(this);
        input.setText(Session.baseUrl());
        input.setHint("http://119.91.65.225:8080/");
        input.setTextSize(13);
        input.setPadding(30, 20, 30, 20);
        new AlertDialog.Builder(this)
                .setTitle("后端地址")
                .setMessage("写词与生成音乐都调用该服务；模拟器用 10.0.2.2，真机填局域网 IP")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (TextUtils.isEmpty(url)) return;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    if (!url.endsWith("/")) url = url + "/";
                    Session.setBaseUrl(url);
                    ApiClient.init(this);
                    refreshServerText();
                    Ui.toast(this, "服务器地址已更新");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ────────────── 参数读取 ──────────────
    private String genre() {
        return GENRE_VALUES[Math.max(0, binding.genreSpinner.getSelectedItemPosition())];
    }

    private String mood() {
        return MOOD_VALUES[Math.max(0, binding.moodSpinner.getSelectedItemPosition())];
    }

    private String vocal() {
        return VOCAL_VALUES[Math.max(0, binding.vocalSpinner.getSelectedItemPosition())];
    }

    private int duration() {
        return DURATION_VALUES[Math.max(0, binding.durationSpinner.getSelectedItemPosition())];
    }

    private int bpm() {
        return 60 + binding.bpmSeek.getProgress();  // 60–150
    }

    /** 写词与生成都要消耗账号积分，未登录时引导去登录。 */
    private boolean requireLogin() {
        if (Session.isLoggedIn()) return true;
        new AlertDialog.Builder(this)
                .setTitle("需要登录")
                .setMessage("AI 写词与生成音乐会消耗账号积分，请先登录")
                .setPositiveButton("去登录", (dialog, which) -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
        return false;
    }

    // ────────────── AI 写词 ──────────────
    private void requestLyrics() {
        if (!requireLogin()) return;
        String title = binding.titleInput.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Ui.toast(this, "请先填写歌曲名称");
            return;
        }
        binding.aiLyricsButton.setEnabled(false);
        binding.aiLyricsButton.setText("写词中…");
        Repository.get().mobileLyrics(title, genre(), mood(), bpm(), new RepoCallback<Models.MobileLyrics>() {
            @Override
            public void onSuccess(Models.MobileLyrics data) {
                binding.aiLyricsButton.setEnabled(true);
                binding.aiLyricsButton.setText("AI 写词");
                if (data == null || TextUtils.isEmpty(data.lyrics)) {
                    Ui.toast(MusicCreateActivity.this, "写词失败：返回为空");
                    return;
                }
                binding.lyricsInput.setText(data.lyrics);
                Ui.toast(MusicCreateActivity.this, "歌词已生成，可直接修改");
            }

            @Override
            public void onError(String message) {
                binding.aiLyricsButton.setEnabled(true);
                binding.aiLyricsButton.setText("AI 写词");
                Ui.toast(MusicCreateActivity.this, "写词失败：" + message);
            }
        });
    }

    // ────────────── 生成歌曲 ──────────────
    private void generate() {
        if (!requireLogin()) return;
        String title = binding.titleInput.getText().toString().trim();
        String lyrics = binding.lyricsInput.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Ui.toast(this, "请先填写歌曲名称");
            return;
        }
        if (!TextUtils.isEmpty(audioUrl)) stopPlayback();

        binding.generateButton.setEnabled(false);
        binding.generateButton.setText("提交中…");
        binding.aiLyricsButton.setEnabled(false);
        binding.playButton.setVisibility(View.GONE);
        binding.resultText.setVisibility(View.GONE);
        binding.genProgress.setProgress(0);
        binding.stageText.setText("任务提交中…");
        audioUrl = null;
        taskId = null;
        handler.removeCallbacks(poller);

        Repository.get().mobileMusic(title, lyrics, genre(), mood(), duration(), vocal(), title,
                new RepoCallback<Models.MobileMusicData>() {
                    @Override
                    public void onSuccess(Models.MobileMusicData data) {
                        if (data == null || TextUtils.isEmpty(data.generationId)) {
                            fail("提交失败：返回为空");
                            return;
                        }
                        taskId = data.generationId;
                        binding.generateButton.setText("生成中…");
                        binding.stageText.setText(data.credits > 0
                                ? "已提交（消耗 " + data.credits + " 积分），正在排队…"
                                : "已提交，正在排队…");
                        handler.postDelayed(poller, 1500);
                    }

                    @Override
                    public void onError(String message) {
                        fail("提交失败：" + message);
                    }
                });
    }

    private String stageText(String stage, String status) {
        String value = TextUtils.isEmpty(stage) ? status : stage;
        if (value == null) return "生成中…";
        switch (value) {
            case "queued":
                return "已排队，等待生成…";
            case "composing":
                return "正在编曲…";
            case "rendering":
                return "正在渲染音频…";
            case "mixing":
                return "正在混音…";
            case "succeeded":
                return "生成完成";
            default:
                return "生成中…";
        }
    }

    private void onFinished(Models.Generation gen) {
        handler.removeCallbacks(poller);
        taskId = null;
        binding.genProgress.setProgress(100);
        binding.stageText.setText("生成完成");
        binding.generateButton.setEnabled(true);
        binding.generateButton.setText("再生成一首");
        binding.aiLyricsButton.setEnabled(true);

        if (TextUtils.isEmpty(gen.workId)) {
            fail("生成完成但没有作品信息");
            return;
        }
        Repository.get().work(gen.workId, new RepoCallback<Models.Work>() {
            @Override
            public void onSuccess(Models.Work work) {
                if (work == null) {
                    fail("生成完成但没有作品信息");
                    return;
                }
                audioUrl = absoluteUrl(work.audioUrl);
                binding.resultText.setText("作品已保存到「我的作品」"
                        + (work.duration > 0 ? " · 时长 " + work.duration + " 秒" : ""));
                binding.resultText.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(audioUrl)) {
                    Ui.toast(MusicCreateActivity.this, "作品没有音频地址，暂时无法试听");
                    return;
                }
                binding.playButton.setVisibility(View.VISIBLE);
                Ui.toast(MusicCreateActivity.this, "歌曲已生成，可以试听");
            }

            @Override
            public void onError(String message) {
                fail("获取作品失败：" + message);
            }
        });
    }

    /** 后端返回的 /media/generated/xxx 补全为可播放的绝对地址。 */
    private String absoluteUrl(String path) {
        if (TextUtils.isEmpty(path)) return "";
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String base = Session.baseUrl();
        if (!base.endsWith("/")) base = base + "/";
        return base + (path.startsWith("/") ? path.substring(1) : path);
    }

    private void fail(String message) {
        handler.removeCallbacks(poller);
        taskId = null;
        binding.generateButton.setEnabled(true);
        binding.generateButton.setText("生成歌曲");
        binding.aiLyricsButton.setEnabled(true);
        binding.stageText.setText("生成失败，请重试");
        Ui.toast(this, message);
    }

    // ────────────── 播放 ──────────────
    private void togglePlay() {
        if (player != null && player.isPlaying()) {
            stopPlayback();
            binding.playButton.setText("播放成品");
            return;
        }
        if (TextUtils.isEmpty(audioUrl)) return;
        stopPlayback();
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            player.setDataSource(audioUrl);
            player.setOnPreparedListener(mp -> {
                mp.start();
                binding.playButton.setText("停止播放");
            });
            player.setOnCompletionListener(mp -> {
                binding.playButton.setText("播放成品");
                stopPlayback();
            });
            player.setOnErrorListener((mp, what, extra) -> {
                Ui.toast(this, "播放失败，请检查服务器地址是否可访问");
                binding.playButton.setText("播放成品");
                stopPlayback();
                return true;
            });
            binding.playButton.setText("加载中…");
            player.prepareAsync();
        } catch (Exception e) {
            Ui.toast(this, "播放失败：" + e.getMessage());
            binding.playButton.setText("播放成品");
            stopPlayback();
        }
    }

    private void stopPlayback() {
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
            } catch (Exception ignored) {
            }
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(poller);
        stopPlayback();
    }
}

package com.sonichub.app.ui.music;

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

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.data.music.MusicClient;
import com.sonichub.app.data.music.MusicModels;
import com.sonichub.app.databinding.ActivityMusicCreateBinding;
import com.sonichub.app.util.Ui;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 「AI 写歌」页面：歌名 → AI 写词（可选）→ 生成歌曲 → 试听。
 * <p>
 * 直接对接本地音乐大模型服务（Python，默认 http://10.0.2.2:8001），
 * 不依赖 App 主后端（8080）的登录/积分体系。
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

    private ActivityMusicCreateBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private String taskId;      // 当前生成任务 ID
    private String audioUrl;    // 成品绝对地址
    private volatile boolean saving;  // 是否正在上传到「我的作品」

    /** 每 2 秒轮询一次任务进度。 */
    private final Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (taskId == null) return;
            MusicClient.api(MusicCreateActivity.this).progress(taskId).enqueue(new Callback<MusicModels.Progress>() {
                @Override
                public void onResponse(Call<MusicModels.Progress> call, Response<MusicModels.Progress> resp) {
                    MusicModels.Progress data = resp.body();
                    if (data == null) {
                        handler.postDelayed(poller, 2000);
                        return;
                    }
                    if (!data.ok) {
                        fail(data.error == null ? "任务不存在" : data.error);
                        return;
                    }
                    binding.genProgress.setProgress(Math.max(0, Math.min(100, data.progress)));
                    binding.stageText.setText(TextUtils.isEmpty(data.msg) ? "生成中…" : data.msg);
                    if ("done".equals(data.status)) {
                        onFinished(data);
                    } else if ("failed".equals(data.status)) {
                        fail(TextUtils.isEmpty(data.error) ? "生成失败，请重试" : data.error);
                    } else {
                        handler.postDelayed(poller, 2000);
                    }
                }

                @Override
                public void onFailure(Call<MusicModels.Progress> call, Throwable t) {
                    fail("查询进度失败：" + t.getMessage());
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
        String base = MusicClient.baseUrl(this)
                .replace("http://", "").replace("https://", "");
        binding.serverText.setText("引擎：" + base + "（点击修改）");
    }

    /** 修改音乐大模型服务地址（模拟器 10.0.2.2，真机填电脑局域网 IP）。 */
    private void editServer() {
        EditText input = new EditText(this);
        input.setText(MusicClient.baseUrl(this));
        input.setHint("http://10.0.2.2:8001/");
        input.setTextSize(13);
        input.setPadding(30, 20, 30, 20);
        new AlertDialog.Builder(this)
                .setTitle("音乐引擎地址")
                .setMessage("模拟器用 10.0.2.2；真机填电脑局域网 IP（如 192.168.1.10:8001）")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (TextUtils.isEmpty(url)) return;
                    MusicClient.setBaseUrl(this, url);
                    refreshServerText();
                    Ui.toast(this, "引擎地址已更新");
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

    // ────────────── AI 写词 ──────────────
    private void requestLyrics() {
        String title = binding.titleInput.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Ui.toast(this, "请先填写歌曲名称");
            return;
        }
        binding.aiLyricsButton.setEnabled(false);
        binding.aiLyricsButton.setText("写词中…");
        MusicClient.api(this)
                .lyrics(new MusicModels.LyricsRequest(title, genre(), mood(), bpm()))
                .enqueue(new Callback<MusicModels.LyricsResponse>() {
                    @Override
                    public void onResponse(Call<MusicModels.LyricsResponse> call,
                                           Response<MusicModels.LyricsResponse> resp) {
                        binding.aiLyricsButton.setEnabled(true);
                        binding.aiLyricsButton.setText("AI 写词");
                        MusicModels.LyricsResponse data = resp.body();
                        if (data == null || !data.ok || TextUtils.isEmpty(data.lyrics)) {
                            String err = data == null ? "返回为空" : (data.error == null ? "生成失败" : data.error);
                            Ui.toast(MusicCreateActivity.this, "写词失败：" + err);
                            return;
                        }
                        binding.lyricsInput.setText(data.lyrics);
                        Ui.toast(MusicCreateActivity.this, "歌词已生成，可直接修改");
                    }

                    @Override
                    public void onFailure(Call<MusicModels.LyricsResponse> call, Throwable t) {
                        binding.aiLyricsButton.setEnabled(true);
                        binding.aiLyricsButton.setText("AI 写词");
                        Ui.toast(MusicCreateActivity.this, "写词失败：" + t.getMessage());
                    }
                });
    }

    // ────────────── 生成歌曲 ──────────────
    private void generate() {
        String title = binding.titleInput.getText().toString().trim();
        String lyrics = binding.lyricsInput.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Ui.toast(this, "请先填写歌曲名称");
            return;
        }
        if (!TextUtils.isEmpty(audioUrl)) stopPlayback();

        MusicModels.GenerateRequest body = new MusicModels.GenerateRequest();
        body.title = title;
        body.lyrics = lyrics;  // 留空 → 服务端自动写词并演唱
        body.genre = genre();
        body.mood = mood();
        body.vocal = vocal();
        body.bpm = bpm();
        body.duration = duration();

        binding.generateButton.setEnabled(false);
        binding.generateButton.setText("提交中…");
        binding.aiLyricsButton.setEnabled(false);
        binding.playButton.setVisibility(View.GONE);
        binding.resultText.setVisibility(View.GONE);
        binding.genProgress.setProgress(0);
        binding.stageText.setText("任务提交中…");
        audioUrl = null;
        taskId = null;

        MusicClient.api(this).generate(body).enqueue(new Callback<MusicModels.GenerateResponse>() {
            @Override
            public void onResponse(Call<MusicModels.GenerateResponse> call,
                                   Response<MusicModels.GenerateResponse> resp) {
                MusicModels.GenerateResponse data = resp.body();
                if (data == null || !data.ok || TextUtils.isEmpty(data.task_id)) {
                    String err = data == null ? "返回为空" : (data.error == null ? "提交失败" : data.error);
                    fail(err);
                    return;
                }
                taskId = data.task_id;
                binding.generateButton.setText("生成中…");
                binding.stageText.setText("已提交，正在排队…");
                handler.removeCallbacks(poller);
                handler.postDelayed(poller, 1500);
            }

            @Override
            public void onFailure(Call<MusicModels.GenerateResponse> call, Throwable t) {
                fail("提交失败：" + t.getMessage());
            }
        });
    }

    private void onFinished(MusicModels.Progress data) {
        handler.removeCallbacks(poller);
        taskId = null;
        binding.genProgress.setProgress(100);
        binding.stageText.setText("生成完成");
        binding.generateButton.setEnabled(true);
        binding.generateButton.setText("再生成一首");
        binding.aiLyricsButton.setEnabled(true);

        MusicModels.Result result = data.result;
        if (result == null || TextUtils.isEmpty(result.wav_url)) {
            fail("生成完成但没有音频地址");
            return;
        }
        audioUrl = MusicClient.absoluteUrl(this, result.wav_url);
        String engine = TextUtils.isEmpty(result.engine) ? "AI 引擎" : result.engine;
        binding.resultText.setText("引擎：" + engine
                + " · 时长 " + Math.round(result.duration_sec) + " 秒"
                + " · 耗时 " + Math.round(result.elapsed_sec) + " 秒");
        binding.resultText.setVisibility(View.VISIBLE);
        binding.playButton.setVisibility(View.VISIBLE);
        Ui.toast(this, "歌曲已生成，可以试听");
        saveToMyWorks(result);
    }

    // ────────────── 保存到「我的作品」 ──────────────
    /** 生成完成后把音频上传到主后端，登记为当前用户的作品。 */
    private void saveToMyWorks(MusicModels.Result result) {
        if (!Session.isLoggedIn()) {
            Ui.toast(this, "未登录，作品未保存到「我的作品」");
            return;
        }
        if (saving || TextUtils.isEmpty(audioUrl)) return;
        saving = true;
        final String title = binding.titleInput.getText().toString().trim();
        final String genre = GENRE_LABELS[Math.max(0, binding.genreSpinner.getSelectedItemPosition())];
        final String mood = MOOD_LABELS[Math.max(0, binding.moodSpinner.getSelectedItemPosition())];
        final String tags = genre + "," + mood;
        final int seconds = (int) Math.round(result.duration_sec > 0 ? result.duration_sec : duration());
        final String url = audioUrl;
        appendResult("正在保存到「我的作品」…");

        okhttp3.OkHttpClient downloader = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        downloader.newCall(new okhttp3.Request.Builder().url(url).build())
                .enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, java.io.IOException e) {
                        onSaveFailed("音频下载失败：" + e.getMessage());
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            onSaveFailed("音频下载失败 (" + response.code() + ")");
                            return;
                        }
                        try {
                            uploadWork(title, genre, tags, seconds, response.body().bytes());
                        } catch (Exception e) {
                            onSaveFailed(e.getMessage());
                        }
                    }
                });
    }

    private void uploadWork(String title, String genre, String tags, int seconds, byte[] audio) {
        Repository.get().importWork(title, genre, tags, seconds, "", audio, new RepoCallback<Models.Work>() {
            @Override
            public void onSuccess(Models.Work data) {
                saving = false;
                appendResult("已保存到「我的作品」");
                Ui.toast(MusicCreateActivity.this, "已保存到「我的作品」");
            }

            @Override
            public void onError(String message) {
                onSaveFailed(message);
            }
        });
    }

    private void onSaveFailed(String message) {
        saving = false;
        appendResult("保存到「我的作品」失败");
        runOnUiThread(() -> Ui.toast(MusicCreateActivity.this, "保存失败：" + message));
    }

    private void appendResult(String text) {
        runOnUiThread(() -> binding.resultText.setText(binding.resultText.getText() + " · " + text));
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
                Ui.toast(this, "播放失败，请检查引擎地址是否可访问");
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

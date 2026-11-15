package com.sonichub.app.ui.player;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sonichub.app.R;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityPlayerBinding;
import com.sonichub.app.databinding.SheetCommentsBinding;
import com.sonichub.app.ui.artist.ArtistActivity;
import com.sonichub.app.util.Ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerActivity extends AppCompatActivity implements PlayerContract.View {
    public static final String EXTRA_WORK_ID = "work_id";
    public static final String EXTRA_TITLE = "title";

    private ActivityPlayerBinding binding;
    private PlayerPresenter presenter;
    private Models.Work current;
    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final Pattern LRC_TAG =
            Pattern.compile("^(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?$");

    private final List<String> lyricTexts = new ArrayList<>();
    private final List<Integer> lyricWeights = new ArrayList<>();
    private final List<Integer> lyricStarts = new ArrayList<>();
    private final List<TextView> lyricViews = new ArrayList<>();
    private boolean lyricsTimed;
    private int activeLyric = -1;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                try {
                    int pos = player.getCurrentPosition();
                    binding.seekBar.setProgress(pos);
                    binding.currentTime.setText(format(pos));
                    syncLyric(pos);
                } catch (Exception ignored) {
                }
            }
            handler.postDelayed(this, 500);
        }
    };

    private BottomSheetDialog commentDialog;
    private SheetCommentsBinding sheetBinding;
    private CommentAdapter commentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new PlayerPresenter(this);

        binding.backButton.setOnClickListener(v -> finish());
        binding.playButton.setOnClickListener(v -> togglePlay());
        binding.artistRow.setOnClickListener(v -> {
            if (current != null && current.ownerId != null) {
                Intent intent = new Intent(this, ArtistActivity.class);
                intent.putExtra(ArtistActivity.EXTRA_ARTIST_ID, current.ownerId);
                startActivity(intent);
            }
        });
        binding.likeButton.setOnClickListener(v -> {
            if (current != null) presenter.like(current);
        });
        binding.collectButton.setOnClickListener(v -> {
            if (current != null) presenter.collect(current);
        });
        binding.commentButton.setOnClickListener(v -> openComments());
        binding.shareButton.setOnClickListener(v -> {
            if (current != null) presenter.share(current.id);
        });
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    player.seekTo(progress);
                    syncLyric(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        String id = getIntent().getStringExtra(EXTRA_WORK_ID);
        if (id == null || id.isEmpty()) {
            Ui.toast(this, "作品不存在");
            finish();
            return;
        }
        presenter.load(id);
    }

    private void togglePlay() {
        if (player == null) {
            Ui.toast(this, "该作品暂无音频，去写一首新歌吧");
            return;
        }
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
            handler.removeCallbacks(ticker);
            handler.post(ticker);
        }
        updatePlayIcon();
    }

    private void prepareAudio(Models.Work work) {
        releasePlayer();
        if (work.audioUrl == null || work.audioUrl.isEmpty()) {
            binding.playButton.setAlpha(0.5f);
            return;
        }
        binding.playButton.setAlpha(1f);
        try {
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            player.setDataSource(Ui.mediaUrl(work.audioUrl));
            player.setOnPreparedListener(mp -> {
                binding.seekBar.setMax(mp.getDuration());
                binding.totalTime.setText(format(mp.getDuration()));
                applyTimeline(mp.getDuration());
                mp.start();
                updatePlayIcon();
                handler.removeCallbacks(ticker);
                handler.post(ticker);
            });
            player.setOnCompletionListener(mp -> {
                updatePlayIcon();
                binding.seekBar.setProgress(0);
                binding.currentTime.setText("00:00");
                handler.removeCallbacks(ticker);
                mp.seekTo(0);
                resetLyric();
            });
            player.prepareAsync();
        } catch (Exception e) {
            Ui.toast(this, "音频加载失败：" + e.getMessage());
        }
    }

    private void updatePlayIcon() {
        boolean playing = player != null && player.isPlaying();
        binding.playButton.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                player.stop();
            } catch (Exception ignored) {
            }
            player.release();
            player = null;
        }
        handler.removeCallbacks(ticker);
    }

    @Override
    public void showWork(Models.Work work) {
        current = work;
        binding.cover.setCover(work.cover);
        binding.title.setText(work.title);
        binding.artistName.setText(work.ownerName);
        binding.artistAvatar.setCover(work.ownerAvatar);
        binding.artistAvatar.setCircle(true);
        binding.likeCount.setText(Ui.count(work.likeCount));
        binding.commentCount.setText(Ui.count(work.commentCount));
        binding.totalTime.setText(work.durationText());
        binding.commentCount.setText(work.commentCount + "");
        updateLike(work);
        updateCollect(work);
        setupLyrics(work);
        prepareAudio(work);
    }

    @Override
    public void onLikeUpdated(Models.Work work) {
        current = work;
        updateLike(work);
    }

    private void updateLike(Models.Work work) {
        int color = ContextCompat.getColor(this, work.liked ? R.color.accent : R.color.text_3);
        binding.likeIcon.setImageTintList(ColorStateList.valueOf(color));
        binding.likeCount.setTextColor(color);
        binding.likeCount.setText(Ui.count(work.likeCount));
    }

    @Override
    public void onCollectUpdated(Models.Work work) {
        current = work;
        updateCollect(work);
        Ui.toast(this, work.collected ? "已收藏" : "已取消收藏");
    }

    private void updateCollect(Models.Work work) {
        int color = ContextCompat.getColor(this, work.collected ? R.color.mint : R.color.text_3);
        binding.collectIcon.setImageTintList(ColorStateList.valueOf(color));
    }

    // ── 歌词：解析、时间轴与逐行高亮 ──

    private void setupLyrics(Models.Work work) {
        binding.lyricsBox.removeAllViews();
        lyricTexts.clear();
        lyricWeights.clear();
        lyricStarts.clear();
        lyricViews.clear();
        activeLyric = -1;
        lyricsTimed = false;

        String raw = work.lyrics == null ? "" : work.lyrics;
        for (String rawLine : raw.replace("\r", "").split("\n")) {
            String text = rawLine.trim();
            if (text.isEmpty()) continue;
            int startMs = -1;
            // 行首的 [mm:ss.xx] 时间戳与 [Verse]/[副歌] 等段落标签一并剥离
            while (text.startsWith("[")) {
                int close = text.indexOf(']');
                if (close <= 0) break;
                Matcher m = LRC_TAG.matcher(text.substring(1, close));
                if (m.matches() && startMs < 0) startMs = lrcMillis(m);
                text = text.substring(close + 1).trim();
            }
            if (text.startsWith("【")) {
                int close = text.indexOf('】');
                if (close > 0) text = text.substring(close + 1).trim();
            }
            if (text.isEmpty()) continue;
            lyricTexts.add(text);
            lyricWeights.add(lyricWeight(text));
            lyricStarts.add(startMs);
            if (startMs >= 0) lyricsTimed = true;
        }

        if (lyricTexts.isEmpty()) {
            binding.lyricsScroll.setVisibility(View.GONE);
            binding.lyricsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        binding.lyricsScroll.setVisibility(View.VISIBLE);
        binding.lyricsEmpty.setVisibility(View.GONE);

        int padH = dp(8);
        for (String text : lyricTexts) {
            TextView line = new TextView(this);
            line.setText(text);
            line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            line.setTextColor(ContextCompat.getColor(this, R.color.text_3));
            line.setAlpha(0.55f);
            line.setGravity(Gravity.CENTER);
            line.setLineSpacing(dp(3), 1f);
            line.setPadding(padH, dp(7), padH, dp(7));
            binding.lyricsBox.addView(line, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            lyricViews.add(line);
        }

        applyTimeline(work.duration > 0 ? work.duration * 1000 : 30000);
        binding.lyricsScroll.post(() -> {
            int half = binding.lyricsScroll.getHeight() / 2;
            if (half > 0) binding.lyricsBox.setPadding(0, half, 0, half);
        });
    }

    /** LRC 有时间戳时直接使用；否则按每行字数在歌曲时长内分摊，贴合逐句演唱的节奏。 */
    private void applyTimeline(int durationMs) {
        if (lyricTexts.isEmpty() || lyricsTimed) return;
        int total = 0;
        for (int weight : lyricWeights) total += weight;
        if (total <= 0) return;
        int duration = Math.max(durationMs, 5000);
        int head = (int) (duration * 0.08);
        int span = Math.max((int) (duration * 0.95) - head, 1000);
        lyricStarts.clear();
        int acc = 0;
        for (int weight : lyricWeights) {
            lyricStarts.add(head + (int) ((long) span * acc / total));
            acc += weight;
        }
    }

    private void syncLyric(int positionMs) {
        if (lyricStarts.isEmpty() || lyricStarts.size() != lyricViews.size()) return;
        int index = -1;
        for (int i = 0; i < lyricStarts.size(); i++) {
            if (positionMs >= lyricStarts.get(i)) index = i;
            else break;
        }
        if (index == activeLyric) return;
        if (activeLyric >= 0 && activeLyric < lyricViews.size()) {
            TextView old = lyricViews.get(activeLyric);
            old.setTextColor(ContextCompat.getColor(this, R.color.text_3));
            old.setAlpha(0.55f);
        }
        activeLyric = index;
        if (index < 0) return;
        TextView line = lyricViews.get(index);
        line.setTextColor(ContextCompat.getColor(this, R.color.accent));
        line.setAlpha(1f);
        int offset = binding.lyricsScroll.getHeight() / 2 - line.getHeight() / 2;
        binding.lyricsScroll.smoothScrollTo(0, Math.max(line.getTop() - offset, 0));
    }

    private void resetLyric() {
        if (activeLyric >= 0 && activeLyric < lyricViews.size()) {
            TextView line = lyricViews.get(activeLyric);
            line.setTextColor(ContextCompat.getColor(this, R.color.text_3));
            line.setAlpha(0.55f);
        }
        activeLyric = -1;
        binding.lyricsScroll.smoothScrollTo(0, 0);
    }

    private static int lrcMillis(Matcher m) {
        int minutes = Integer.parseInt(m.group(1));
        int seconds = Integer.parseInt(m.group(2));
        int millis = 0;
        String frac = m.group(3);
        if (frac != null) {
            millis = Integer.parseInt(frac);
            if (frac.length() == 2) millis *= 10;
            else if (frac.length() == 1) millis *= 100;
        }
        return (minutes * 60 + seconds) * 1000 + millis;
    }

    private static int lyricWeight(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) count++;
        }
        return Math.max(4, count);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void openComments() {
        if (current == null) return;
        sheetBinding = SheetCommentsBinding.inflate(getLayoutInflater());
        commentAdapter = new CommentAdapter();
        sheetBinding.commentList.setLayoutManager(new LinearLayoutManager(this));
        sheetBinding.commentList.setAdapter(commentAdapter);
        commentDialog = new BottomSheetDialog(this);
        commentDialog.setContentView(sheetBinding.getRoot());
        commentDialog.show();
        presenter.loadComments(current.id);
        sheetBinding.commentSend.setOnClickListener(v -> {
            String text = sheetBinding.commentInput.getText().toString().trim();
            if (text.isEmpty()) return;
            presenter.addComment(current.id, text);
            sheetBinding.commentInput.setText("");
        });
    }

    @Override
    public void showComments(List<Models.Comment> items) {
        if (commentAdapter != null) commentAdapter.setItems(items);
    }

    @Override
    public void appendComment(Models.Comment item) {
        if (commentAdapter != null) {
            commentAdapter.append(item);
            if (sheetBinding != null) {
                sheetBinding.commentList.smoothScrollToPosition(commentAdapter.getItemCount() - 1);
            }
        }
        Ui.toast(this, "评论已发布");
    }

    @Override
    public void showShare(String url) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("SonicHub", url));
        }
        Ui.toast(this, "分享链接已复制");
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
    }

    private String format(int ms) {
        int total = ms / 1000;
        return String.format(Locale.US, "%02d:%02d", total / 60, total % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}

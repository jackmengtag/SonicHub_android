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
import android.widget.SeekBar;

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

import java.util.List;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity implements PlayerContract.View {
    public static final String EXTRA_WORK_ID = "work_id";
    public static final String EXTRA_TITLE = "title";

    private ActivityPlayerBinding binding;
    private PlayerPresenter presenter;
    private Models.Work current;
    private MediaPlayer player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                try {
                    int pos = player.getCurrentPosition();
                    binding.seekBar.setProgress(pos);
                    binding.currentTime.setText(format(pos));
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
                if (fromUser && player != null) player.seekTo(progress);
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
                mp.start();
                updatePlayIcon();
                handler.removeCallbacks(ticker);
                handler.post(ticker);
            });
            player.setOnCompletionListener(mp -> {
                updatePlayIcon();
                binding.seekBar.setProgress(0);
                binding.currentTime.setText("00:00");
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

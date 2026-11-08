package com.sonichub.app.ui.artist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityArtistBinding;
import com.sonichub.app.ui.discover.WorkAdapter;
import com.sonichub.app.ui.player.PlayerActivity;
import com.sonichub.app.util.Ui;

public class ArtistActivity extends AppCompatActivity implements ArtistContract.View {
    public static final String EXTRA_ARTIST_ID = "artist_id";

    private ActivityArtistBinding binding;
    private ArtistPresenter presenter;
    private WorkAdapter adapter;
    private Models.UserCard currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new ArtistPresenter(this);

        adapter = new WorkAdapter(new WorkAdapter.Listener() {
            @Override
            public void onWork(Models.Work work) {
                Intent intent = new Intent(ArtistActivity.this, PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_WORK_ID, work.id);
                intent.putExtra(PlayerActivity.EXTRA_TITLE, work.title);
                startActivity(intent);
            }

            @Override
            public void onLike(Models.Work work) {
                presenter.like(work);
            }
        });
        binding.worksList.setLayoutManager(new LinearLayoutManager(this));
        binding.worksList.setAdapter(adapter);
        binding.backButton.setOnClickListener(v -> finish());
        binding.followButton.setOnClickListener(v -> {
            if (currentUser != null) presenter.follow(currentUser);
        });

        String id = getIntent().getStringExtra(EXTRA_ARTIST_ID);
        if (id == null || id.isEmpty()) {
            Ui.toast(this, "音乐人不存在");
            finish();
            return;
        }
        presenter.load(id);
    }

    @Override
    public void showArtist(Models.ArtistData data) {
        if (data.user == null) return;
        currentUser = data.user;
        binding.banner.setCover(data.user.avatar);
        binding.avatar.setCover(data.user.avatar);
        binding.avatar.setCircle(true);
        binding.name.setText(data.user.name);
        binding.stage.setText(data.musician != null && data.musician.stageName != null
                ? data.musician.stageName : "音乐人");
        binding.bio.setText(data.user.bio);
        int count = data.works == null ? 0 : data.works.size();
        binding.stats.setText(count + " 作品 · " + (data.user.verified ? "已认证" : "未认证"));
        binding.followButton.setText(data.user.following ? "已关注" : "关注");
        binding.followButton.setVisibility(data.user.id.equals(Session.userId())
                ? android.view.View.GONE : android.view.View.VISIBLE);
        adapter.setItems(data.works);
    }

    @Override
    public void onFollowUpdated(boolean following) {
        binding.followButton.setText(following ? "已关注" : "关注");
        Ui.toast(this, following ? "已关注" : "已取消关注");
    }

    @Override
    public void onLikeUpdated(Models.Work work) {
        adapter.update(work);
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
    }
}

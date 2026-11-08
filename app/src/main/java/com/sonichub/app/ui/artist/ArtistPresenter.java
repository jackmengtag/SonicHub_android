package com.sonichub.app.ui.artist;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;

public class ArtistPresenter implements ArtistContract.Presenter {
    private final ArtistContract.View view;
    private final Repository repository;

    public ArtistPresenter(ArtistContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(String artistId) {
        repository.artist(artistId, new RepoCallback<Models.ArtistData>() {
            @Override
            public void onSuccess(Models.ArtistData data) {
                view.showArtist(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void follow(Models.UserCard user) {
        if (!Session.isLoggedIn()) {
            view.showError("请先登录后再关注");
            return;
        }
        repository.follow(user.id, user.following, new RepoCallback<Models.FollowData>() {
            @Override
            public void onSuccess(Models.FollowData data) {
                user.following = data.following;
                view.onFollowUpdated(data.following);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void like(Models.Work work) {
        if (!Session.isLoggedIn()) {
            view.showError("请先登录后再点赞");
            return;
        }
        repository.like(work.id, work.liked, new RepoCallback<Models.LikeData>() {
            @Override
            public void onSuccess(Models.LikeData data) {
                work.liked = data.liked;
                work.likeCount = data.likeCount;
                view.onLikeUpdated(work);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

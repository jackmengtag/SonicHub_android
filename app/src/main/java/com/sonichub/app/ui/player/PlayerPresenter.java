package com.sonichub.app.ui.player;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;

public class PlayerPresenter implements PlayerContract.Presenter {
    private final PlayerContract.View view;
    private final Repository repository;

    public PlayerPresenter(PlayerContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(String workId) {
        repository.work(workId, new RepoCallback<Models.Work>() {
            @Override
            public void onSuccess(Models.Work data) {
                view.showWork(data);
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

    @Override
    public void collect(Models.Work work) {
        if (!Session.isLoggedIn()) {
            view.showError("请先登录后再收藏");
            return;
        }
        repository.collect(work.id, work.collected, new RepoCallback<Models.ActionData>() {
            @Override
            public void onSuccess(Models.ActionData data) {
                work.collected = !work.collected;
                view.onCollectUpdated(work);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void loadComments(String workId) {
        repository.comments(workId, new RepoCallback<Models.CommentsData>() {
            @Override
            public void onSuccess(Models.CommentsData data) {
                view.showComments(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void addComment(String workId, String content) {
        repository.addComment(workId, content, new RepoCallback<Models.Comment>() {
            @Override
            public void onSuccess(Models.Comment data) {
                view.appendComment(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void share(String workId) {
        repository.shareLink(workId, new RepoCallback<Models.ShareData>() {
            @Override
            public void onSuccess(Models.ShareData data) {
                String base = com.sonichub.app.data.Session.baseUrl();
                if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
                view.showShare(base + "/api/v1/s/" + data.code);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

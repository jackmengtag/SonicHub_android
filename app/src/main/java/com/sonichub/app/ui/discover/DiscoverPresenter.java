package com.sonichub.app.ui.discover;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiscoverPresenter implements DiscoverContract.Presenter {
    private final DiscoverContract.View view;
    private final Repository repository;
    private boolean loading;

    public DiscoverPresenter(DiscoverContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(boolean following) {
        if (loading) return;
        loading = true;
        if (following) {
            repository.following(20, 0, new RepoCallback<Models.Page<Models.Work>>() {
                @Override
                public void onSuccess(Models.Page<Models.Work> data) {
                    loading = false;
                    List<Models.Work> items = data.items == null ? new ArrayList<>() : data.items;
                    view.showWorks(items);
                    view.showArtists(extractArtists(items));
                }

                @Override
                public void onError(String message) {
                    loading = false;
                    view.showError(message);
                }
            });
        } else {
            repository.recommend(20, 0, new RepoCallback<Models.RecommendData>() {
                @Override
                public void onSuccess(Models.RecommendData data) {
                    loading = false;
                    List<Models.Work> items = data.items == null ? new ArrayList<>() : data.items;
                    view.showWorks(items);
                    view.showArtists(extractArtists(items));
                }

                @Override
                public void onError(String message) {
                    loading = false;
                    view.showError(message);
                }
            });
        }
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

    private List<Models.UserCard> extractArtists(List<Models.Work> works) {
        Map<String, Models.UserCard> map = new LinkedHashMap<>();
        for (Models.Work work : works) {
            if (work.ownerId == null || map.containsKey(work.ownerId)) continue;
            Models.UserCard card = new Models.UserCard();
            card.id = work.ownerId;
            card.name = work.ownerName;
            card.avatar = work.ownerAvatar;
            card.verified = work.ownerVerified;
            card.stageName = "音乐人";
            map.put(card.id, card);
            if (map.size() >= 8) break;
        }
        return new ArrayList<>(map.values());
    }
}

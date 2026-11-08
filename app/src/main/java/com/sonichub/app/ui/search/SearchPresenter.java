package com.sonichub.app.ui.search;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;

public class SearchPresenter implements SearchContract.Presenter {
    private final SearchContract.View view;
    private final Repository repository;

    public SearchPresenter(SearchContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) return;
        repository.search(query.trim(), new RepoCallback<Models.SearchData>() {
            @Override
            public void onSuccess(Models.SearchData data) {
                view.showResults(data);
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

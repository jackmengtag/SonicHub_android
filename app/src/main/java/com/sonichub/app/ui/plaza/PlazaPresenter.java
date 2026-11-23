package com.sonichub.app.ui.plaza;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;

public class PlazaPresenter implements PlazaContract.Presenter {
    private final PlazaContract.View view;
    private final Repository repository;
    private int requestSeq;

    public PlazaPresenter(PlazaContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(String sort, String genre) {
        final int seq = ++requestSeq;
        repository.plaza(sort, genre, 30, 0, new RepoCallback<Models.Page<Models.Work>>() {
            @Override
            public void onSuccess(Models.Page<Models.Work> data) {
                if (seq != requestSeq) return;
                view.showWorks(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                if (seq != requestSeq) return;
                view.showError(message);
            }
        });
    }

    @Override
    public void loadGenres() {
        repository.genres(new RepoCallback<Models.GenreData>() {
            @Override
            public void onSuccess(Models.GenreData data) {
                view.showGenres(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showGenres(new ArrayList<>());
            }
        });
    }
}

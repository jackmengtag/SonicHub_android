package com.sonichub.app.ui.plaza;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;
import java.util.List;

public class PlazaPresenter implements PlazaContract.Presenter {
    private final PlazaContract.View view;
    private final Repository repository;
    private boolean loading;

    public PlazaPresenter(PlazaContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(String sort, String genre) {
        if (loading) return;
        loading = true;
        repository.plaza(sort, genre, 30, 0, new RepoCallback<Models.Page<Models.Work>>() {
            @Override
            public void onSuccess(Models.Page<Models.Work> data) {
                loading = false;
                view.showWorks(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                loading = false;
                view.showError(message);
            }
        });
    }
}

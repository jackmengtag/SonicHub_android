package com.sonichub.app.ui.verify;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.List;

public class VerifyPresenter implements VerifyContract.Presenter {
    private final VerifyContract.View view;
    private final Repository repository;

    public VerifyPresenter(VerifyContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load() {
        repository.me(new RepoCallback<Models.MeData>() {
            @Override
            public void onSuccess(Models.MeData data) {
                view.showStatus(data.musician);
            }

            @Override
            public void onError(String message) {
                view.showStatus(null);
            }
        });
    }

    @Override
    public void apply(String stageName, String bio, List<String> genres) {
        repository.applyMusician(stageName, bio, genres, new RepoCallback<Models.ActionData>() {
            @Override
            public void onSuccess(Models.ActionData data) {
                view.onApplied();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

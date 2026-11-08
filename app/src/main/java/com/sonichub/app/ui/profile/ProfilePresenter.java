package com.sonichub.app.ui.profile;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;

public class ProfilePresenter implements ProfileContract.Presenter {
    private final ProfileContract.View view;
    private final Repository repository;

    public ProfilePresenter(ProfileContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load() {
        if (!Session.isLoggedIn()) {
            view.showGuest();
            return;
        }
        repository.me(new RepoCallback<Models.MeData>() {
            @Override
            public void onSuccess(Models.MeData data) {
                view.showMe(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

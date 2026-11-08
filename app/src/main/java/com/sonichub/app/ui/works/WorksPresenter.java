package com.sonichub.app.ui.works;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;

public class WorksPresenter implements WorksContract.Presenter {
    private final WorksContract.View view;
    private final Repository repository;

    public WorksPresenter(WorksContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(String status) {
        repository.myWorks(status, new RepoCallback<Models.Page<Models.Work>>() {
            @Override
            public void onSuccess(Models.Page<Models.Work> data) {
                view.showWorks(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

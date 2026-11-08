package com.sonichub.app.ui.works;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface WorksContract {
    interface View {
        void showWorks(List<Models.Work> works);

        void showError(String message);
    }

    interface Presenter {
        void load(String status);
    }
}

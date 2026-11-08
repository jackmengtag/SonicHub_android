package com.sonichub.app.ui.plaza;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface PlazaContract {
    interface View {
        void showWorks(List<Models.Work> works);

        void showError(String message);
    }

    interface Presenter {
        void load(String sort, String genre);
    }
}

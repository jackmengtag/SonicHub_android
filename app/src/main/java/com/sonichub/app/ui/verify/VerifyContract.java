package com.sonichub.app.ui.verify;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface VerifyContract {
    interface View {
        void showStatus(Models.Musician musician);

        void onApplied();

        void showError(String message);
    }

    interface Presenter {
        void load();

        void apply(String stageName, String bio, List<String> genres);
    }
}

package com.sonichub.app.ui.profile;

import com.sonichub.app.data.model.Models;

public interface ProfileContract {
    interface View {
        void showMe(Models.MeData data);

        void showGuest();

        void showError(String message);
    }

    interface Presenter {
        void load();
    }
}

package com.sonichub.app.ui.discover;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface DiscoverContract {
    interface View {
        void showWorks(List<Models.Work> works);

        void showArtists(List<Models.UserCard> artists);

        void showError(String message);

        void onLikeUpdated(Models.Work work);
    }

    interface Presenter {
        void load(boolean following);

        void like(Models.Work work);
    }
}

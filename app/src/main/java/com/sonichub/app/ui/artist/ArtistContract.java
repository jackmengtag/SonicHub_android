package com.sonichub.app.ui.artist;

import com.sonichub.app.data.model.Models;

public interface ArtistContract {
    interface View {
        void showArtist(Models.ArtistData data);

        void onFollowUpdated(boolean following);

        void onLikeUpdated(Models.Work work);

        void showError(String message);
    }

    interface Presenter {
        void load(String artistId);

        void follow(Models.UserCard user);

        void like(Models.Work work);
    }
}

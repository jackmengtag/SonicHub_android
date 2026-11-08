package com.sonichub.app.ui.search;

import com.sonichub.app.data.model.Models;

public interface SearchContract {
    interface View {
        void showResults(Models.SearchData data);

        void onLikeUpdated(Models.Work work);

        void showError(String message);
    }

    interface Presenter {
        void search(String query);

        void like(Models.Work work);
    }
}

package com.sonichub.app.ui.player;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface PlayerContract {
    interface View {
        void showWork(Models.Work work);

        void onLikeUpdated(Models.Work work);

        void onCollectUpdated(Models.Work work);

        void showComments(List<Models.Comment> items);

        void appendComment(Models.Comment item);

        void showShare(String url);

        void showError(String message);
    }

    interface Presenter {
        void load(String workId);

        void like(Models.Work work);

        void collect(Models.Work work);

        void loadComments(String workId);

        void addComment(String workId, String content);

        void share(String workId);
    }
}

package com.sonichub.app.ui.messages;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;

public class MessagesPresenter implements MessagesContract.Presenter {
    private final MessagesContract.View view;
    private final Repository repository;

    public MessagesPresenter(MessagesContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void loadNotifications() {
        repository.notifications(null, new RepoCallback<Models.NotificationsData>() {
            @Override
            public void onSuccess(Models.NotificationsData data) {
                view.showNotifications(data.items == null ? new ArrayList<>() : data.items, data.unread);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void loadConversations() {
        repository.conversations(new RepoCallback<Models.ConversationsData>() {
            @Override
            public void onSuccess(Models.ConversationsData data) {
                view.showConversations(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void readAll() {
        repository.readAllNotifications(new RepoCallback<Models.ActionData>() {
            @Override
            public void onSuccess(Models.ActionData data) {
                loadNotifications();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

package com.sonichub.app.ui.chat;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.model.Models;

import java.util.ArrayList;

public class ChatPresenter implements ChatContract.Presenter {
    private final ChatContract.View view;
    private final Repository repository;

    public ChatPresenter(ChatContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void load(String conversationId) {
        repository.messages(conversationId, new RepoCallback<Models.MessagesData>() {
            @Override
            public void onSuccess(Models.MessagesData data) {
                view.showMessages(data.items == null ? new ArrayList<>() : data.items);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void send(String conversationId, String text) {
        repository.sendMessage(conversationId, text, new RepoCallback<Models.Message>() {
            @Override
            public void onSuccess(Models.Message data) {
                view.appendMessage(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
}

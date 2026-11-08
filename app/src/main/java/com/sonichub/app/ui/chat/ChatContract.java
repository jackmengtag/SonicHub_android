package com.sonichub.app.ui.chat;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface ChatContract {
    interface View {
        void showMessages(List<Models.Message> items);

        void appendMessage(Models.Message item);

        void showError(String message);
    }

    interface Presenter {
        void load(String conversationId);

        void send(String conversationId, String text);
    }
}

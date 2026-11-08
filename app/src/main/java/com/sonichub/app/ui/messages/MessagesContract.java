package com.sonichub.app.ui.messages;

import com.sonichub.app.data.model.Models;

import java.util.List;

public interface MessagesContract {
    interface View {
        void showNotifications(List<Models.Notification> items, int unread);

        void showConversations(List<Models.Conversation> items);

        void showError(String message);
    }

    interface Presenter {
        void loadNotifications();

        void loadConversations();

        void readAll();
    }
}

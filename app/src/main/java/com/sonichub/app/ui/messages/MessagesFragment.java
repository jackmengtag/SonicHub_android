package com.sonichub.app.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.R;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.FragmentMessagesBinding;
import com.sonichub.app.ui.artist.ArtistActivity;
import com.sonichub.app.ui.chat.ChatActivity;
import com.sonichub.app.ui.player.PlayerActivity;
import com.sonichub.app.util.Ui;

import java.util.List;

public class MessagesFragment extends Fragment implements MessagesContract.View {
    private FragmentMessagesBinding binding;
    private MessagesPresenter presenter;
    private NotificationAdapter notificationAdapter;
    private ConversationAdapter conversationAdapter;
    private boolean conversationsTab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        presenter = new MessagesPresenter(this);
        notificationAdapter = new NotificationAdapter(item -> {
            presenter.readAll();
            if ("work".equals(item.targetType) && item.targetId != null) {
                Intent intent = new Intent(requireContext(), PlayerActivity.class);
                intent.putExtra(PlayerActivity.EXTRA_WORK_ID, item.targetId);
                startActivity(intent);
            } else if ("user".equals(item.targetType) && item.targetId != null) {
                Intent intent = new Intent(requireContext(), ArtistActivity.class);
                intent.putExtra(ArtistActivity.EXTRA_ARTIST_ID, item.targetId);
                startActivity(intent);
            }
        });
        conversationAdapter = new ConversationAdapter(item -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_ID, item.id);
            intent.putExtra(ChatActivity.EXTRA_NAME, item.peerName);
            startActivity(intent);
        });
        binding.messageList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.messageList.setAdapter(notificationAdapter);

        binding.tabNotifications.setOnClickListener(v -> selectTab(false));
        binding.tabConversations.setOnClickListener(v -> selectTab(true));
        binding.readAll.setOnClickListener(v -> presenter.readAll());

        if (!Session.isLoggedIn()) {
            Ui.toast(requireContext(), "登录后可查看消息");
            return;
        }
        selectTab(false);
    }

    private void selectTab(boolean conversations) {
        conversationsTab = conversations;
        if (conversations) {
            binding.tabNotifications.setBackground(null);
            binding.tabConversations.setBackgroundResource(R.drawable.bg_segment_selected);
            binding.messageList.setAdapter(conversationAdapter);
            presenter.loadConversations();
        } else {
            binding.tabNotifications.setBackgroundResource(R.drawable.bg_segment_selected);
            binding.tabConversations.setBackground(null);
            binding.messageList.setAdapter(notificationAdapter);
            presenter.loadNotifications();
        }
        binding.tabNotifications.setTextColor(getResources().getColor(conversations ? R.color.text_3 : R.color.text));
        binding.tabConversations.setTextColor(getResources().getColor(conversations ? R.color.text : R.color.text_3));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && Session.isLoggedIn() && !conversationsTab) {
            presenter.loadNotifications();
        }
    }

    @Override
    public void showNotifications(List<Models.Notification> items, int unread) {
        notificationAdapter.setItems(items);
    }

    @Override
    public void showConversations(List<Models.Conversation> items) {
        conversationAdapter.setItems(items);
    }

    @Override
    public void showError(String message) {
        Ui.toast(requireContext(), message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

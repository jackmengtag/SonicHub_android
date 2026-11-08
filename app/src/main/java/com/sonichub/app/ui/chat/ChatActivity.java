package com.sonichub.app.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sonichub.app.databinding.ActivityChatBinding;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.util.Ui;

import java.util.List;

public class ChatActivity extends AppCompatActivity implements ChatContract.View {
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_NAME = "name";

    private ActivityChatBinding binding;
    private ChatPresenter presenter;
    private MessageAdapter adapter;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        conversationId = getIntent().getStringExtra(EXTRA_CONVERSATION_ID);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        binding.name.setText(name == null ? "私信" : name);

        presenter = new ChatPresenter(this);
        adapter = new MessageAdapter();
        binding.messageList.setLayoutManager(new LinearLayoutManager(this));
        binding.messageList.setAdapter(adapter);

        binding.backButton.setOnClickListener(v -> finish());
        binding.sendButton.setOnClickListener(v -> send());
        presenter.load(conversationId);
    }

    private void send() {
        String text = binding.input.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        binding.input.setText("");
        presenter.send(conversationId, text);
    }

    @Override
    public void showMessages(List<Models.Message> items) {
        adapter.setItems(items);
        scrollToBottom();
    }

    @Override
    public void appendMessage(Models.Message item) {
        adapter.append(item);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            binding.messageList.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
    }
}

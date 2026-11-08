package com.sonichub.app.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.sonichub.app.data.ApiClient;
import com.sonichub.app.data.Navigator;
import com.sonichub.app.data.Session;
import com.sonichub.app.databinding.ActivityLoginBinding;
import com.sonichub.app.util.Ui;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {
    private ActivityLoginBinding binding;
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Session.isLoggedIn()) {
            Navigator.toMain(this);
            return;
        }
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        presenter = new LoginPresenter(this);

        binding.loginButton.setOnClickListener(v -> {
            if (!binding.agreeCheck.isChecked()) {
                Ui.toast(this, "请先阅读并同意用户协议");
                return;
            }
            presenter.login(binding.phoneInput.getText().toString().trim(),
                    binding.codeInput.getText().toString().trim());
        });
        binding.sendCode.setOnClickListener(v -> {
            Ui.toast(this, "验证码已发送（演示验证码 1234）");
            binding.codeInput.setText("1234");
        });
        binding.guestButton.setOnClickListener(v -> Navigator.toMain(this));

        binding.serverText.setText("服务器：" + displayBaseUrl() + "（点击修改）");
        binding.serverText.setOnClickListener(v -> editServer());
    }

    private String displayBaseUrl() {
        return Session.baseUrl().replace("http://", "").replace("https://", "");
    }

    private void editServer() {
        EditText input = new EditText(this);
        input.setText(Session.baseUrl());
        input.setHint("http://192.168.1.10:8080/");
        input.setTextSize(13);
        input.setPadding(30, 20, 30, 20);
        new AlertDialog.Builder(this)
                .setTitle("后端地址")
                .setMessage("模拟器使用 10.0.2.2；真机请填写电脑局域网 IP")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (TextUtils.isEmpty(url)) return;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "http://" + url;
                    }
                    if (!url.endsWith("/")) url = url + "/";
                    Session.setBaseUrl(url);
                    ApiClient.init(this);
                    binding.serverText.setText("服务器：" + displayBaseUrl() + "（点击修改）");
                    Ui.toast(this, "服务器地址已更新");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void showLoading(boolean loading) {
        binding.loginButton.setEnabled(!loading);
        binding.loginButton.setText(loading ? "登录中…" : "登录 / 注册");
    }

    @Override
    public void showError(String message) {
        Ui.toast(this, message);
    }

    @Override
    public void loginSuccess() {
        Navigator.toMain(this);
    }
}

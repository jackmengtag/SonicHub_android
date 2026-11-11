package com.sonichub.app.ui.login;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nirvana.prd.sms.auth.ResultCode;
import com.nirvana.prd.sms.auth.SmsAuthHelper;
import com.nirvana.prd.sms.auth.TokenUpdater;
import com.nirvana.prd.sms.auth.Tokens;
import com.nirvana.prd.sms.auth.utils.PackageUtils;
import com.sonichub.app.data.ApiClient;
import com.sonichub.app.data.Navigator;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;
import com.sonichub.app.databinding.ActivityLoginBinding;
import com.sonichub.app.ui.music.MusicCreateActivity;
import com.sonichub.app.util.Ui;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements LoginContract.View, TokenUpdater {
    private ActivityLoginBinding binding;
    private LoginPresenter presenter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int countdown;
    private SmsAuthHelper smsAuthHelper;
    private String smsVerifyToken;  // 短信认证 SDK 发送成功后返回的 smsToken

    /** 验证码重发倒计时。 */
    private final Runnable countdownTick = new Runnable() {
        @Override
        public void run() {
            countdown--;
            if (countdown <= 0) {
                binding.sendCode.setEnabled(true);
                binding.sendCode.setAlpha(1f);
                binding.sendCode.setText(getString(com.sonichub.app.R.string.send_code));
                return;
            }
            binding.sendCode.setText(countdown + "s 后重发");
            handler.postDelayed(this, 1000);
        }
    };

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
            String phone = binding.phoneInput.getText().toString().trim();
            String code = binding.codeInput.getText().toString().trim();
            if (!TextUtils.isEmpty(smsVerifyToken)) {
                // 短信认证 SDK 发送的验证码，走 SDK 核验并直接登录
                presenter.verifySmsCode(phone, code, smsVerifyToken);
            } else {
                presenter.login(phone, code);
            }
        });
        binding.sendCode.setOnClickListener(v -> sendCode());
        binding.oneClickButton.setOnClickListener(v -> showOneClickHint());
        binding.guestButton.setOnClickListener(v -> Navigator.toMain(this));
        // 免登录体验：直接进本地音乐大模型的 AI 写歌页
        binding.aiCreateEntry.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, MusicCreateActivity.class)));

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

    // ────────────── 短信验证码（认证 SDK 优先，失败回退普通短信通道） ──────────────
    private static final long TOKEN_CACHE_MILLIS = 4 * 60 * 1000L;
    private Models.SmsTokens cachedTokenData;
    private long cachedTokenAt;

    private void sendCode() {
        String phone = binding.phoneInput.getText().toString().trim();
        if (phone.length() != 11) {
            Ui.toast(this, "请输入 11 位手机号");
            return;
        }
        binding.sendCode.setEnabled(false);
        binding.sendCode.setAlpha(0.6f);
        binding.sendCode.setText("发送中…");
        new Thread(() -> {
            final Models.SmsTokens data = fetchTokenData();
            runOnUiThread(() -> {
                if (data == null || TextUtils.isEmpty(data.sceneCode)) {
                    resetSendCodeButton();
                    Ui.toast(this, "短信认证方案未配置，改用普通短信通道");
                    presenter.sendCode(phone);
                    return;
                }
                initSmsSdk(data.sceneCode);
                smsAuthHelper.sendVerifyCode(86, phone, ret -> runOnUiThread(() -> {
                    if (ret != null && ResultCode.CODE_SUCCESS.equals(ret.getCode())) {
                        smsVerifyToken = ret.getSmsVerifyToken();
                        Ui.toast(this, "验证码已发送，请注意查收");
                        startCountdown(60);
                    } else {
                        resetSendCodeButton();
                        String reason = ret == null ? "未知错误" : ret.getMsg();
                        Ui.toast(this, "短信认证通道不可用（" + reason + "），改用普通短信");
                        presenter.sendCode(phone);
                    }
                }), 5000);
            });
        }).start();
    }

    private void resetSendCodeButton() {
        binding.sendCode.setEnabled(true);
        binding.sendCode.setAlpha(1f);
        binding.sendCode.setText(getString(com.sonichub.app.R.string.send_code));
    }

    private void initSmsSdk(String sceneCode) {
        if (smsAuthHelper == null) {
            smsAuthHelper = new SmsAuthHelper(this, sceneCode);
            smsAuthHelper.setTokenUpdater(this);
        }
    }

    /** 认证 SDK 在子线程调用：返回 BizToken 与 STS 临时凭证。 */
    @Override
    public Tokens updateToken() {
        Models.SmsTokens data = fetchTokenData();
        if (data == null) {
            return null;
        }
        Tokens tokens = new Tokens();
        tokens.setBizToken(data.bizToken);
        tokens.setAccessKeyId(data.stsAccessKeyId);
        tokens.setAccessKeySecret(data.stsAccessKeySecret);
        tokens.setStsToken(data.stsToken);
        tokens.setExpiredTimeMills(data.expireTime);
        return tokens;
    }

    /** 同步向后端 /auth/sms/tokens 换取凭证（带 4 分钟本地缓存）。 */
    private Models.SmsTokens fetchTokenData() {
        if (cachedTokenData != null && System.currentTimeMillis() - cachedTokenAt < TOKEN_CACHE_MILLIS) {
            return cachedTokenData;
        }
        try {
            String base = Session.baseUrl();
            if (!base.endsWith("/")) base = base + "/";
            JSONObject body = new JSONObject();
            body.put("scene_code", "");
            body.put("package_name", PackageUtils.getPackageName(this));
            body.put("sign_name", PackageUtils.getSign(this));
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(base + "api/v1/auth/sms/tokens")
                    .post(okhttp3.RequestBody.create(body.toString(),
                            okhttp3.MediaType.parse("application/json; charset=utf-8")))
                    .build();
            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return null;
                }
                Models.ApiResponse<Models.SmsTokens> parsed = new Gson().fromJson(
                        response.body().string(),
                        new TypeToken<Models.ApiResponse<Models.SmsTokens>>() {
                        }.getType());
                if (parsed == null || parsed.data == null) {
                    return null;
                }
                cachedTokenData = parsed.data;
                cachedTokenAt = System.currentTimeMillis();
                return parsed.data;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void codeSent(Models.SmsSendData data) {
        if (data == null) return;
        if (data.demo) {
            if (!TextUtils.isEmpty(data.demoCode)) {
                binding.codeInput.setText(data.demoCode);
            }
            Ui.toast(this, TextUtils.isEmpty(data.message)
                    ? "演示模式：验证码 " + data.demoCode : data.message);
        } else {
            binding.codeInput.setText("");
            Ui.toast(this, TextUtils.isEmpty(data.message) ? "验证码已发送，请注意查收" : data.message);
        }
        startCountdown(data.intervalSeconds > 0 ? data.intervalSeconds : 60);
    }

    private void startCountdown(int seconds) {
        handler.removeCallbacks(countdownTick);
        countdown = seconds;
        binding.sendCode.setEnabled(false);
        binding.sendCode.setAlpha(0.6f);
        binding.sendCode.setText(seconds + "s 后重发");
        handler.postDelayed(countdownTick, 1000);
    }

    /** 一键登录入口：号码认证 SDK 授权成功后调用 loginWithOneClickToken。 */
    private void showOneClickHint() {
        new AlertDialog.Builder(this)
                .setTitle("本机号码一键登录")
                .setMessage("需要集成阿里云号码认证 Android SDK，并在后端配置 AccessKey、短信签名与模板后才能使用。\n\n"
                        + "当前环境未集成 SDK，请先使用短信验证码登录。")
                .setPositiveButton("知道了", null)
                .show();
    }

    /** 供号码认证 SDK 授权回调调用：用一次性 AccessToken 换取手机号并登录。 */
    public void loginWithOneClickToken(String accessToken) {
        presenter.oneClickLogin(accessToken);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(countdownTick);
        if (smsAuthHelper != null) {
            smsAuthHelper.destroy();
            smsAuthHelper = null;
        }
        super.onDestroy();
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

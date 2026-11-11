package com.sonichub.app.ui.login;

import com.sonichub.app.data.RepoCallback;
import com.sonichub.app.data.Repository;
import com.sonichub.app.data.Session;
import com.sonichub.app.data.model.Models;

public class LoginPresenter implements LoginContract.Presenter {
    private final LoginContract.View view;
    private final Repository repository;

    public LoginPresenter(LoginContract.View view) {
        this.view = view;
        this.repository = Repository.get();
    }

    @Override
    public void login(String phone, String code) {
        if (phone == null || phone.length() != 11) {
            view.showError("请输入 11 位手机号");
            return;
        }
        if (code == null || code.length() < 4) {
            view.showError("请输入验证码");
            return;
        }
        view.showLoading(true);
        repository.login(phone, code, new RepoCallback<Models.LoginData>() {
            @Override
            public void onSuccess(Models.LoginData data) {
                view.showLoading(false);
                Session.save(data);
                view.loginSuccess();
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showError(message);
            }
        });
    }

    @Override
    public void sendCode(String phone) {
        if (phone == null || phone.length() != 11) {
            view.showError("请输入 11 位手机号");
            return;
        }
        repository.sendSms(phone, new RepoCallback<Models.SmsSendData>() {
            @Override
            public void onSuccess(Models.SmsSendData data) {
                view.codeSent(data);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    @Override
    public void oneClickLogin(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            view.showError("未获取到运营商授权凭证");
            return;
        }
        view.showLoading(true);
        repository.oneClickLogin(accessToken, new RepoCallback<Models.LoginData>() {
            @Override
            public void onSuccess(Models.LoginData data) {
                view.showLoading(false);
                Session.save(data);
                view.loginSuccess();
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showError(message);
            }
        });
    }

    @Override
    public void verifySmsCode(String phone, String code, String smsToken) {
        if (phone == null || phone.length() != 11) {
            view.showError("请输入 11 位手机号");
            return;
        }
        if (code == null || code.isEmpty() || smsToken == null || smsToken.isEmpty()) {
            view.showError("请先获取验证码");
            return;
        }
        view.showLoading(true);
        repository.smsVerify(phone, code, smsToken, new RepoCallback<Models.LoginData>() {
            @Override
            public void onSuccess(Models.LoginData data) {
                view.showLoading(false);
                Session.save(data);
                view.loginSuccess();
            }

            @Override
            public void onError(String message) {
                view.showLoading(false);
                view.showError(message);
            }
        });
    }
}

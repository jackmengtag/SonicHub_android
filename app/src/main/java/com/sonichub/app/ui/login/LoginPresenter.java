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
            view.showError("请输入验证码，演示可填 1234");
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
}

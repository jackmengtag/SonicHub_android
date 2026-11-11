package com.sonichub.app.ui.login;

import com.sonichub.app.data.model.Models;

public interface LoginContract {
    interface View {
        void showLoading(boolean loading);

        void showError(String message);

        void loginSuccess();

        void codeSent(Models.SmsSendData data);
    }

    interface Presenter {
        void login(String phone, String code);

        void sendCode(String phone);

        void oneClickLogin(String accessToken);

        void verifySmsCode(String phone, String code, String smsToken);
    }
}

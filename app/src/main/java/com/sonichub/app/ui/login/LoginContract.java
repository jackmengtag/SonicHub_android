package com.sonichub.app.ui.login;

public interface LoginContract {
    interface View {
        void showLoading(boolean loading);

        void showError(String message);

        void loginSuccess();
    }

    interface Presenter {
        void login(String phone, String code);
    }
}

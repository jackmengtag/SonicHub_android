package com.sonichub.app.data;

import android.app.Activity;

import com.sonichub.app.R;
import com.sonichub.app.ui.login.LoginActivity;
import com.sonichub.app.ui.main.MainActivity;

public final class Navigator {
    private Navigator() {
    }

    public static void toMain(Activity activity) {
        activity.startActivity(new android.content.Intent(activity, MainActivity.class));
        activity.finish();
    }

    public static void toLogin(Activity activity) {
        activity.startActivity(new android.content.Intent(activity, LoginActivity.class));
        activity.finish();
    }
}

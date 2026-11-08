package com.sonichub.app;

import android.app.Application;

import com.sonichub.app.data.ApiClient;
import com.sonichub.app.data.Session;

public class SonicHubApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Session.init(this);
        ApiClient.init(this);
    }
}

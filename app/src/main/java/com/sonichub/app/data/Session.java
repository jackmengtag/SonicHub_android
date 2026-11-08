package com.sonichub.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.sonichub.app.BuildConfig;
import com.sonichub.app.data.model.Models;

public final class Session {
    private static SharedPreferences prefs;

    private Session() {
    }

    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("sonichub", Context.MODE_PRIVATE);
    }

    public static String baseUrl() {
        return prefs.getString("base_url", BuildConfig.API_BASE_URL);
    }

    public static void setBaseUrl(String url) {
        prefs.edit().putString("base_url", url).apply();
    }

    public static void save(Models.LoginData data) {
        prefs.edit()
                .putString("access_token", data.accessToken)
                .putString("refresh_token", data.refreshToken)
                .putString("user_id", data.user != null ? data.user.id : "")
                .putString("user_name", data.user != null ? data.user.displayName : "")
                .putString("avatar", data.user != null ? data.user.avatar : "c6")
                .putString("tenant_id", data.tenantId)
                .apply();
    }

    public static String token() {
        return prefs.getString("access_token", "");
    }

    public static void updateTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .apply();
    }

    public static String refreshToken() {
        return prefs.getString("refresh_token", "");
    }

    public static String userId() {
        return prefs.getString("user_id", "");
    }

    public static String userName() {
        return prefs.getString("user_name", "");
    }

    public static String avatar() {
        return prefs.getString("avatar", "c6");
    }

    public static String tenantId() {
        return prefs.getString("tenant_id", "");
    }

    public static boolean isLoggedIn() {
        return !token().isEmpty();
    }

    public static void clear() {
        String base = baseUrl();
        prefs.edit().clear().putString("base_url", base).apply();
    }
}

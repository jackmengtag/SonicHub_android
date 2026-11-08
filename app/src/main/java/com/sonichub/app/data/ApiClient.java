package com.sonichub.app.data;

import android.content.Context;

import com.sonichub.app.BuildConfig;
import com.sonichub.app.data.model.Models;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static final Object REFRESH_LOCK = new Object();
    private static ApiService service;

    private ApiClient() {
    }

    public static void init(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient base = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        String baseUrl = Session.baseUrl();
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";

        // 刷新令牌使用独立客户端，避免 401 时递归触发 Authenticator。
        ApiService authApi = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(base)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        OkHttpClient client = base.newBuilder()
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder();
                    String token = Session.token();
                    if (!token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .authenticator((route, response) -> {
                    // 登录/刷新本身不重试；同一次请求最多重试一次。
                    String path = response.request().url().encodedPath();
                    if (path.endsWith("/auth/login") || path.endsWith("/auth/refresh")) {
                        return null;
                    }
                    if (response.request().header("Authorization") == null || attempts(response) > 1) {
                        return null;
                    }
                    synchronized (REFRESH_LOCK) {
                        String used = response.request().header("Authorization");
                        String current = Session.token();
                        if (!current.isEmpty() && !("Bearer " + current).equals(used)) {
                            return retryWith(response, current);
                        }
                        Models.LoginData data = refreshSync(authApi);
                        if (data == null || data.accessToken == null || data.accessToken.isEmpty()) {
                            Session.clear();
                            return null;
                        }
                        Session.updateTokens(data.accessToken, data.refreshToken);
                        return retryWith(response, data.accessToken);
                    }
                })
                .build();

        service = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    private static Request retryWith(Response response, String token) {
        return response.request().newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private static int attempts(Response response) {
        int count = 0;
        while (response != null) {
            count++;
            response = response.priorResponse();
        }
        return count;
    }

    private static Models.LoginData refreshSync(ApiService authApi) {
        String refreshToken = Session.refreshToken();
        if (refreshToken.isEmpty()) {
            return null;
        }
        try {
            retrofit2.Response<Models.ApiResponse<Models.LoginData>> response =
                    authApi.refresh(new Models.RefreshRequest(refreshToken)).execute();
            if (response.isSuccessful() && response.body() != null && response.body().ok()) {
                return response.body().data;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ApiService api() {
        return service;
    }
}

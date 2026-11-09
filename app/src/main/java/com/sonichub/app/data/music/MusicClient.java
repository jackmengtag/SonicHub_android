package com.sonichub.app.data.music;

import android.content.Context;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 音乐大模型服务的 Retrofit 客户端。
 * <p>
 * 与本 App 主后端（8080）完全独立：地址存在独立 SharedPreferences 里，
 * 默认 {@code http://10.0.2.2:8001/}（模拟器访问宿主机），真机可在页面里改成电脑局域网 IP。
 */
public final class MusicClient {
    public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8001/";  // 模拟器默认地址
    private static final String PREFS = "music_engine";  // 独立偏好文件，互不影响主后端配置
    private static final String KEY_BASE = "base_url";

    private MusicClient() {
    }

    /** 读取当前音乐引擎地址（已规范化为以 / 结尾）。 */
    public static String baseUrl(Context context) {
        String value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_BASE, DEFAULT_BASE_URL);
        return normalize(value);
    }

    /** 保存音乐引擎地址。 */
    public static void setBaseUrl(Context context, String url) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_BASE, normalize(url)).apply();
    }

    /** 规范化：补 http:// 前缀、补结尾 /，保证 Retrofit baseUrl 合法。 */
    public static String normalize(String url) {
        String value = TextUtils.isEmpty(url) ? DEFAULT_BASE_URL : url.trim();
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            value = "http://" + value;
        }
        if (!value.endsWith("/")) {
            value = value + "/";
        }
        return value;
    }

    /** 按当前配置创建接口实例。 */
    public static MusicApi api(Context context) {
        return api(baseUrl(context));
    }

    /** 按指定地址创建接口实例（写词冷启动最长约 60 秒，读超时给足 5 分钟）。 */
    public static MusicApi api(String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        return new Retrofit.Builder()
                .baseUrl(normalize(baseUrl))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MusicApi.class);
    }

    /** 把服务端返回的 /output/xxx.wav 拼成可播放的绝对地址。 */
    public static String absoluteUrl(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String base = baseUrl(context);
        return base + (path.startsWith("/") ? path.substring(1) : path);
    }
}

package com.sonichub.app.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.sonichub.app.BuildConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class Ui {
    private Ui() {
    }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static void toast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static String count(int n) {
        if (n >= 10000) {
            return String.format(Locale.US, "%.1f万", n / 10000f).replace(".0万", "万");
        }
        if (n >= 1000) {
            return String.format(Locale.US, "%.1fk", n / 1000f).replace(".0k", "k");
        }
        return String.valueOf(n);
    }

    public static String mediaUrl(String path) {
        if (path == null || path.isEmpty()) return "";
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        String base = BuildConfig.API_BASE_URL;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!path.startsWith("/")) path = "/" + path;
        return base + path;
    }

    public static String relativeTime(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        String value = iso;
        if (value.length() > 19) value = value.substring(0, 19);
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = parser.parse(value);
            if (date == null) return "";
            long diff = System.currentTimeMillis() - date.getTime();
            long minutes = diff / 60000L;
            if (minutes < 1) return "刚刚";
            if (minutes < 60) return minutes + " 分钟前";
            long hours = minutes / 60;
            if (hours < 24) return hours + " 小时前";
            long days = hours / 24;
            if (days < 30) return days + " 天前";
            return new SimpleDateFormat("MM-dd", Locale.US).format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    public static void visible(View view, boolean show) {
        if (view != null) view.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

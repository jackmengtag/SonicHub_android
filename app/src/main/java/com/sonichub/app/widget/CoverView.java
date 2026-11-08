package com.sonichub.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.sonichub.app.util.Ui;

import java.util.HashMap;
import java.util.Map;

public class CoverView extends View {
    private static final Map<String, int[]> COLORS = new HashMap<>();

    static {
        COLORS.put("c1", new int[]{0xFFFFB36B, 0xFFE0562A, 0xFF5A1B0E});
        COLORS.put("c2", new int[]{0xFF9BE7FF, 0xFF2F6E8C, 0xFF10222E});
        COLORS.put("c3", new int[]{0xFFC8F2C0, 0xFF4E8F5E, 0xFF16301F});
        COLORS.put("c4", new int[]{0xFFFFC9D8, 0xFFB9536F, 0xFF41141F});
        COLORS.put("c5", new int[]{0xFFAFC7FF, 0xFF445C9E, 0xFF141B31});
        COLORS.put("c6", new int[]{0xFFE8D3A3, 0xFF94724A, 0xFF2C2015});
        COLORS.put("c7", new int[]{0xFFFFA9A0, 0xFFA93A36, 0xFF37100F});
        COLORS.put("c8", new int[]{0xFFB6FFE4, 0xFF2E8C74, 0xFF0C2B25});
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path clip = new Path();
    private String cover = "c1";
    private boolean circle;

    public CoverView(Context context) {
        this(context, null);
    }

    public CoverView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(Ui.dp(context, 1));
        ringPaint.setColor(0x33FFFFFF);
    }

    public void setCover(String cover) {
        this.cover = cover == null ? "c1" : cover;
        invalidate();
    }

    public void setCircle(boolean circle) {
        this.circle = circle;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float radius = circle ? Math.min(w, h) / 2f : Ui.dp(getContext(), 14);
        rect.set(0, 0, w, h);
        clip.reset();
        if (circle) {
            clip.addCircle(w / 2f, h / 2f, Math.min(w, h) / 2f, Path.Direction.CW);
        } else {
            clip.addRoundRect(rect, radius, radius, Path.Direction.CW);
        }
        canvas.save();
        canvas.clipPath(clip);

        int[] colors = COLORS.get(cover);
        if (colors == null) colors = COLORS.get("c1");
        paint.setShader(new LinearGradient(0, 0, w, h, colors, null, Shader.TileMode.CLAMP));
        canvas.drawRect(rect, paint);

        paint.setShader(new RadialGradient(w * 0.82f, h * 0.12f, w * 0.72f,
                new int[]{0x55FFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(rect, paint);

        paint.setShader(null);
        float cx = w * 0.56f;
        float cy = h * 0.56f;
        float max = Math.min(w, h) * 0.40f;
        for (int i = 1; i <= 4; i++) {
            canvas.drawCircle(cx, cy, max * i / 4f, ringPaint);
        }
        ringPaint.setStyle(Paint.Style.FILL);
        ringPaint.setColor(0x66000000);
        canvas.drawCircle(cx, cy, Math.min(w, h) * 0.045f, ringPaint);
        ringPaint.setStyle(Paint.Style.STROKE);

        paint.setShader(new LinearGradient(0, 0, 0, h, 0x00000000, 0x66000000, Shader.TileMode.CLAMP));
        canvas.drawRect(rect, paint);
        paint.setShader(null);
        canvas.restore();

        if (!circle) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dp(getContext(), 1));
            paint.setColor(0x1AFFFFFF);
            rect.set(0.5f, 0.5f, w - 0.5f, h - 0.5f);
            canvas.drawRoundRect(rect, radius, radius, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}

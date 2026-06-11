package com.fasting.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

/**
 * 圆形倒计时进度视图
 * 用于显示断食/进食的圆形倒计时
 */
public class CircularProgressView extends View {

    // 绘制相关的 Paint
    private final Paint bgArcPaint;
    private final Paint progressArcPaint;
    private final Paint timeTextPaint;
    private final Paint labelTextPaint;

    // 绘制区域
    private final RectF arcRect = new RectF();
    private final Rect textBounds = new Rect();

    // 属性
    private float progress = 0f;         // 0.0 ~ 1.0
    private String timeText = "00:00:00";
    private String labelText = "准备开始";
    private int progressColor;
    private int bgArcColor;
    private int labelColor;

    // 动画
    private ValueAnimator progressAnimator;
    private float animatedProgress = 0f;

    // 尺寸常量 (dp)
    private final float strokeWidth;
    private final float timeTextSize;
    private final float labelTextSize;

    public CircularProgressView(Context context) {
        this(context, null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = context.getResources().getDisplayMetrics().density;
        strokeWidth = 20f * density;
        timeTextSize = 48f * density;
        labelTextSize = 16f * density;

        // 初始化默认颜色
        progressColor = ContextCompat.getColor(context, R.color.fasting_color);
        bgArcColor = ContextCompat.getColor(context, R.color.progress_bg);
        labelColor = ContextCompat.getColor(context, R.color.text_secondary);

        // 背景弧画笔
        bgArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgArcPaint.setStyle(Paint.Style.STROKE);
        bgArcPaint.setStrokeWidth(strokeWidth);
        bgArcPaint.setStrokeCap(Paint.Cap.ROUND);
        bgArcPaint.setColor(bgArcColor);

        // 进度弧画笔
        progressArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressArcPaint.setStyle(Paint.Style.STROKE);
        progressArcPaint.setStrokeWidth(strokeWidth);
        progressArcPaint.setStrokeCap(Paint.Cap.ROUND);
        progressArcPaint.setColor(progressColor);

        // 时间文字画笔
        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setTextSize(timeTextSize);
        timeTextPaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        timeTextPaint.setTextAlign(Paint.Align.CENTER);
        timeTextPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

        // 标签文字画笔
        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setTextSize(labelTextSize);
        labelTextPaint.setColor(labelColor);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - strokeWidth / 2f - 10f;

        // 设置弧线绘制区域
        arcRect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        // 绘制背景弧（完整圆）
        canvas.drawArc(arcRect, -90, 360, false, bgArcPaint);

        // 绘制进度弧（从顶部开始，顺时针）
        float sweepAngle = animatedProgress * 360f;
        if (sweepAngle > 0f) {
            canvas.drawArc(arcRect, -90, sweepAngle, false, progressArcPaint);
        }

        // 绘制时间文字（居中）
        float textY = centerY - (timeTextPaint.descent() + timeTextPaint.ascent()) / 2f;
        canvas.drawText(timeText, centerX, textY, timeTextPaint);

        // 绘制标签文字（时间下方）
        float labelY = centerY + radius * 0.45f;
        canvas.drawText(labelText, centerX, labelY, labelTextPaint);
    }

    // ==================== 属性设置 ====================

    /** 设置进度 (0.0~1.0)，带平滑动画 */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        animateProgress(this.progress);
    }

    /** 直接设置进度（无动画） */
    public void setProgressImmediate(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        this.animatedProgress = this.progress;
        invalidate();
    }

    /** 设置显示的时间文字 */
    public void setTimeText(String timeText) {
        this.timeText = timeText;
        invalidate();
    }

    /** 设置标签文字 */
    public void setLabelText(String labelText) {
        this.labelText = labelText;
        invalidate();
    }

    /** 设置进度颜色 */
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressArcPaint.setColor(color);
        invalidate();
    }

    /** 设置标签颜色 */
    public void setLabelColor(int color) {
        this.labelColor = color;
        labelTextPaint.setColor(color);
        invalidate();
    }

    // ==================== 动画 ====================

    private void animateProgress(float targetProgress) {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        progressAnimator = ValueAnimator.ofFloat(animatedProgress, targetProgress);
        progressAnimator.setDuration(300);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        progressAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
    }
}

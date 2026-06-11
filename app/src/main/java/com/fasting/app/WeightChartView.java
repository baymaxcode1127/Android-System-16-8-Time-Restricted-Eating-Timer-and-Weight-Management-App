package com.fasting.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.List;

/**
 * 体重变化曲线图（自定义 View）
 */
public class WeightChartView extends View {

    private final Paint linePaint;
    private final Paint pointPaint;
    private final Paint gridPaint;
    private final Paint textPaint;
    private final Paint refLinePaint;
    private final Paint fillPaint;
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final Rect textBounds = new Rect();

    private List<WeightDatabaseHelper.WeightRecord> records;
    private float minWeight, maxWeight;
    private int paddingLeft, paddingTop, paddingRight, paddingBottom;
    private float chartLeft, chartTop, chartRight, chartBottom;

    public WeightChartView(Context context) {
        this(context, null);
    }

    public WeightChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = context.getResources().getDisplayMetrics().density;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f * density);
        linePaint.setColor(ContextCompat.getColor(context, R.color.chart_line));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(ContextCompat.getColor(context, R.color.chart_point));

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f * density);
        gridPaint.setColor(ContextCompat.getColor(context, R.color.chart_grid));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(11f * density);
        textPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        textPaint.setTextAlign(Paint.Align.CENTER);

        refLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        refLinePaint.setStyle(Paint.Style.STROKE);
        refLinePaint.setStrokeWidth(2f * density);
        refLinePaint.setColor(ContextCompat.getColor(context, R.color.chart_ref_line));
        refLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(ContextCompat.getColor(context, R.color.fasting_light));
    }

    public void setData(List<WeightDatabaseHelper.WeightRecord> records) {
        this.records = records;
        if (records != null && !records.isEmpty()) {
            minWeight = Float.MAX_VALUE;
            maxWeight = Float.MIN_VALUE;
            for (WeightDatabaseHelper.WeightRecord r : records) {
                if (r.weight < minWeight) minWeight = r.weight;
                if (r.weight > maxWeight) maxWeight = r.weight;
            }
            // 扩展范围以便图表好看
            float range = maxWeight - minWeight;
            if (range < 1f) range = 1f;
            minWeight -= range * 0.2f;
            maxWeight += range * 0.2f;
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        paddingLeft = (int) (50f * density);
        paddingTop = (int) (20f * density);
        paddingRight = (int) (20f * density);
        paddingBottom = (int) (40f * density);

        chartLeft = paddingLeft;
        chartTop = paddingTop;
        chartRight = w - paddingRight;
        chartBottom = h - paddingBottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (records == null || records.size() < 2) {
            // 数据不足，显示提示
            String tip = "需要至少两条记录才能显示图表";
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(tip, getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        drawGrid(canvas);
        drawReferenceLine(canvas);
        drawLineAndPoints(canvas);
        drawLabels(canvas);
    }

    private void drawGrid(Canvas canvas) {
        // 水平网格线（5条）
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            float y = chartTop + (chartBottom - chartTop) * i / gridLines;
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);

            // Y轴标签（体重值）
            float weight = maxWeight - (maxWeight - minWeight) * i / gridLines;
            String label = String.format("%.1f", weight);
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(label, chartLeft - 8f * getResources().getDisplayMetrics().density, y + 4f, textPaint);
        }
    }

    private void drawReferenceLine(Canvas canvas) {
        // 初始体重参考线
        if (!records.isEmpty()) {
            float firstWeight = records.get(0).weight;
            float y = chartBottom - (firstWeight - minWeight) / (maxWeight - minWeight) * (chartBottom - chartTop);
            canvas.drawLine(chartLeft, y, chartRight, y, refLinePaint);
            String label = "初始 " + String.format("%.1fkg", firstWeight);
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(label, chartLeft + 8f, y - 8f, textPaint);
        }
    }

    private void drawLineAndPoints(Canvas canvas) {
        linePath.reset();
        fillPath.reset();

        float pointRadius = 6f * getResources().getDisplayMetrics().density;
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;
        int count = records.size();

        for (int i = 0; i < count; i++) {
            float x = chartLeft + chartWidth * i / (count - 1);
            float weight = records.get(i).weight;
            float y = chartBottom - (weight - minWeight) / (maxWeight - minWeight) * chartHeight;

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, chartBottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            // 如果是最后一个点，闭合填充路径
            if (i == count - 1) {
                fillPath.lineTo(x, chartBottom);
                fillPath.close();
            }

            // 绘制数据点
            canvas.drawCircle(x, y, pointRadius, pointPaint);
        }

        // 绘制填充区域
        canvas.drawPath(fillPath, fillPaint);
        // 绘制折线
        canvas.drawPath(linePath, linePaint);
    }

    private void drawLabels(Canvas canvas) {
        // X轴日期标签
        float chartWidth = chartRight - chartLeft;
        int count = records.size();
        float density = getResources().getDisplayMetrics().density;

        textPaint.setTextAlign(Paint.Align.CENTER);
        // 如果点太多，只显示部分标签
        int step = Math.max(1, count / 6);

        for (int i = 0; i < count; i += step) {
            float x = chartLeft + chartWidth * i / (count - 1);
            String date = records.get(i).date;
            // 只显示月-日
            if (date.length() >= 10) {
                date = date.substring(5); // MM-dd
            }
            canvas.drawText(date, x, chartBottom + 18f * density, textPaint);
        }

        // 最后一个点始终显示
        if (count > 1 && (count - 1) % step != 0) {
            float x = chartRight;
            String date = records.get(count - 1).date;
            if (date.length() >= 10) date = date.substring(5);
            canvas.drawText(date, x, chartBottom + 18f * density, textPaint);
        }
    }
}

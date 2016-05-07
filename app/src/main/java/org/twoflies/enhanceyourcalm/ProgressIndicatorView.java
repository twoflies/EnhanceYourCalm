package org.twoflies.enhanceyourcalm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProgressIndicatorView extends View {

    public static int DEFAULT_INDICATOR_COLOR = Color.WHITE;
    public static int DEFAULT_PROGRESS_COLOR = Color.RED;
    public static int DEFAULT_STROKE_WIDTH = 5;

    private int mIndicatorColor;
    private int mProgressColor;
    private int mStrokeWidth;
    private Paint mIndicatorPaint;
    private Paint mProgressPaint;
    private float mCenterX;
    private float mCenterY;
    private float mRadius;
    private RectF mArcOval;
    private float mProgress;
    private float mProgressSweep;

    public ProgressIndicatorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray array = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.ProgressIndicatorView, 0, 0);
        try {
            mIndicatorColor = array.getColor(R.styleable.ProgressIndicatorView_indicatorColor, DEFAULT_INDICATOR_COLOR);
            mProgressColor = array.getColor(R.styleable.ProgressIndicatorView_progressColor, DEFAULT_PROGRESS_COLOR);
            mStrokeWidth = array.getInt(R.styleable.ProgressIndicatorView_strokeWidth, DEFAULT_STROKE_WIDTH);
        } finally {
            array.recycle();
        }

        init();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        mProgress = Math.max(0, Math.min(1, progress));
        mProgressSweep = mProgress * 360;
        postInvalidate();
    }

    public void setComplete(boolean complete) {
        setProgress(complete ? 1.0f : 0.0f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(specWidth, specHeight);

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xPadding = getPaddingLeft() + getPaddingRight();
        float yPadding = getPaddingTop() + getPaddingBottom();
        float totalWidth = w - xPadding - mStrokeWidth;
        float totalHeight = h - yPadding - mStrokeWidth;
        float diameter = Math.min(totalWidth, totalHeight);

        mRadius = diameter / 2;
        float left = getPaddingLeft() + (mStrokeWidth / 2);
        float top = getPaddingTop() + (mStrokeWidth / 2);
        mArcOval = new RectF(left, top, left + diameter, top + diameter);
        mCenterX = mArcOval.centerX();
        mCenterY = mArcOval.centerY();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(mCenterX, mCenterY, mRadius, mIndicatorPaint);
        canvas.drawArc(mArcOval, 270, -mProgressSweep, false, mProgressPaint);
    }

    private void init() {
        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setColor(mIndicatorColor);
        mIndicatorPaint.setStrokeWidth(mStrokeWidth);
        //mIndicatorPaint.setPathEffect(new DashPathEffect(new float[] { mStrokeWidth * 5, mStrokeWidth }, 0));

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStrokeWidth(mStrokeWidth);
    }
}

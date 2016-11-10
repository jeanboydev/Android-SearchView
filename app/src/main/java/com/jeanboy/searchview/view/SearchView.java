package com.jeanboy.searchview.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jeanboy on 2016/11/9.
 */

public class SearchView extends View {

    private int mWidth, mHeight;
    private int glassRadius = 50;
    private int searchRadius = glassRadius * 2;
    private float[] glassBarEnd = new float[2];
    private Path mGlassPath = new Path();
    private Path mSearchPath = new Path();
    private PathMeasure pathMeasure = new PathMeasure();
    private Paint mPaint = new Paint();
    public final static int COLOR_PAINT = Color.WHITE;//图标颜色
    public final static int COLOR_BG = 0xFF0082D7;//背景颜色
    private int nowState = STATE_NONE;

    //定义状态
    public final static int STATE_NONE = 0x000;
    public final static int STATE_LOAD_START = 0x001;
    public final static int STATE_LOADING = 0x002;
    public final static int STATE_LOAD_END = 0x003;
    //默认动画时长
    private final static int ANIM_DURATION = 1500;
    private float currentValue = 0f;
    private ValueAnimator.AnimatorUpdateListener updateListener;
    private ValueAnimator startValueAnimator;
    private ValueAnimator loadingValueAnimator;
    private ValueAnimator endValueAnimator;
    private boolean isSearching = false;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mPaint.setColor(COLOR_PAINT);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(15f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        initPath();
        initListener();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.drawColor(COLOR_BG);
        switch (nowState) {
            case STATE_NONE:
                canvas.drawPath(mGlassPath, mPaint);
                break;
            case STATE_LOAD_START:
                pathMeasure.setPath(mGlassPath, false);
                Path glassStartDst = new Path();
                //获取绘制路径区间，从0-1
                pathMeasure.getSegment(pathMeasure.getLength() * currentValue, pathMeasure.getLength(), glassStartDst, true);
                canvas.drawPath(glassStartDst, mPaint);
                break;
            case STATE_LOADING:
                pathMeasure.setPath(mSearchPath, false);
                Path searchDst = new Path();
                float stop = pathMeasure.getLength() * currentValue;
                float start = (float) (stop - ((0.5 - Math.abs(currentValue - 0.5)) * searchRadius * 2));
                pathMeasure.getSegment(start, stop, searchDst, true);
                canvas.drawPath(searchDst, mPaint);
                break;
            case STATE_LOAD_END:
                pathMeasure.setPath(mGlassPath, false);
                Path glassEndDst = new Path();
                //获取绘制路径区间，从1-0
                pathMeasure.getSegment(pathMeasure.getLength() * currentValue, pathMeasure.getLength(), glassEndDst, true);
                canvas.drawPath(glassEndDst, mPaint);
                break;
        }
    }


    private void initPath() {
        //放大镜路径
        RectF glassRectF = new RectF(-glassRadius, -glassRadius, glassRadius, glassRadius);
        mGlassPath.addArc(glassRectF, 45, 359.9f);//不要设置360°内部会自动优化，测量不能取到需要的数值
        //搜索路径
        RectF searchRectF = new RectF(-searchRadius, -searchRadius, searchRadius, searchRadius);
        mSearchPath.addArc(searchRectF, 45, 359.9f);
        //获取放大镜把的结束坐标，也就是搜索路径的结束坐标
        PathMeasure searchMeasure = new PathMeasure(mSearchPath, false);
        searchMeasure.getPosTan(0, glassBarEnd, null);

        mGlassPath.lineTo(glassBarEnd[0], glassBarEnd[1]);
    }

    private void initListener() {
        updateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentValue = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        };
        startValueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(ANIM_DURATION);
        loadingValueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(ANIM_DURATION);
        loadingValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        loadingValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        endValueAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(ANIM_DURATION);

        startValueAnimator.addUpdateListener(updateListener);
        loadingValueAnimator.addUpdateListener(updateListener);
        endValueAnimator.addUpdateListener(updateListener);

        startValueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                nowState = STATE_LOADING;
                loadingValueAnimator.start();
            }
        });
        endValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isSearching = false;
                nowState = STATE_NONE;
            }
        });
    }

    public boolean isSearching() {
        return isSearching;
    }

    public void startSearch() {
        if (isSearching) return;
        isSearching = true;
        nowState = STATE_LOAD_START;
        startValueAnimator.start();
    }

    public void stopSearch() {
        if (startValueAnimator.isRunning()) {
            startValueAnimator.end();
        }
        if (loadingValueAnimator.isRunning()) {
            loadingValueAnimator.end();
        }
        nowState = STATE_LOAD_END;
        endValueAnimator.start();
    }
}

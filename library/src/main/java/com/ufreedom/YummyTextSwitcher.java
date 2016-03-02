package com.ufreedom;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Author SunMeng
 * Date : 2016 三月 01
 */
public class YummyTextSwitcher extends View {

    public static final int FRAME_NUMBER_MIDDLE = 30;
    public static final int FRAME_NUMBER_START = 3;
    public static final int FRAME_NUMBER_END = 3;


    private static final String TAG = "YummyTextSwitcher";
    private float mTextSize;
    private Paint mTextPaint;
    private int mTextColor;
    private FrameInterpolator mFrameInterpolator;


    private BlurMaskFilter mMaskFilterFirst;
    private BlurMaskFilter mMaskFilterSecond;
    private BlurMaskFilter mMaskFilterMiddle;
    private float scrollY = 0.0f;

    private Paint testPaint;

    private Paint mMiddleFramePaint;
    private Paint mFirstFramePaint;
    private Paint mSecondFramePaint;
    private Rect drawRect = new Rect();

    private float mFrameOffset;

    public YummyTextSwitcher(Context context) {
        this(context, null);
    }

    public YummyTextSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YummyTextSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.YummyTextSwitcher, 0, 0);
        mTextSize = arr.getDimensionPixelSize(R.styleable.YummyTextSwitcher_textSize, 80);
        mTextColor = arr.getColor(R.styleable.YummyTextSwitcher_textColor, Color.BLACK);
        if (arr.hasValue(R.styleable.YummyTextSwitcher_frameOffset)) {
            mFrameOffset = arr.getFloat(R.styleable.YummyTextSwitcher_frameOffset, -1.0f);
        }
        arr.recycle();
        init();

    }

    private void init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //View从API Level 11才加入setLayerType方法
            //设置myView以软件渲染模式绘图
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFrameInterpolator = new NumberFrameInterpolator();

        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mFirstFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFirstFramePaint.setTextSize(mTextSize);
        mFirstFramePaint.setAntiAlias(true);
        mFirstFramePaint.setTextSize(mTextSize);
        mFirstFramePaint.setColor(mTextColor);
        mFirstFramePaint.setStyle(Paint.Style.FILL);
        mFirstFramePaint.setTextAlign(Paint.Align.CENTER);

        mSecondFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondFramePaint.setTextSize(mTextSize);
        mSecondFramePaint.setAntiAlias(true);
        mSecondFramePaint.setTextSize(mTextSize);
        mSecondFramePaint.setColor(mTextColor);
        mSecondFramePaint.setStyle(Paint.Style.FILL);
        mSecondFramePaint.setTextAlign(Paint.Align.CENTER);

        mMiddleFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMiddleFramePaint.setTextSize(mTextSize);
        mMiddleFramePaint.setAntiAlias(true);
        mMiddleFramePaint.setTextSize(mTextSize);
        mMiddleFramePaint.setColor(mTextColor);
        mMiddleFramePaint.setStyle(Paint.Style.FILL);
        mMiddleFramePaint.setTextAlign(Paint.Align.CENTER);
        
        mMaskFilterFirst = new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL);
        mMaskFilterSecond = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        mMaskFilterMiddle = new BlurMaskFilter(25, BlurMaskFilter.Blur.NORMAL);
        
        mFirstFramePaint.setMaskFilter(mMaskFilterFirst);
        mSecondFramePaint.setMaskFilter(mMaskFilterSecond);
        mMiddleFramePaint.setMaskFilter(mMaskFilterMiddle);
        
        testPaint = new Paint();
        testPaint.setStyle(Paint.Style.FILL);
        testPaint.setColor(Color.WHITE);
        
        if (mFrameOffset <= 0) {
            Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
            mFrameOffset = fmi.bottom - fmi.top;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mFrameInterpolator == null){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int widthPaddingOffset =   getPaddingLeft() + getPaddingRight();
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            float maxWidth = getDesireWidth(mTextPaint, mFrameInterpolator);
            Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
            setMeasuredDimension((int) maxWidth + widthPaddingOffset, fmi.bottom - fmi.top);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            float maxWidth = getDesireWidth(mTextPaint, mFrameInterpolator);
            setMeasuredDimension((int) maxWidth + widthPaddingOffset, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
            setMeasuredDimension(widthSize + widthPaddingOffset, fmi.bottom - fmi.top);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    private static float getDesireWidth(Paint mTextPaint, FrameInterpolator mFrameInterpolator) {
        float maxWidth = 0;

        for (int i = 0; i < FRAME_NUMBER_START; i++) {
            float width = mTextPaint.measureText(mFrameInterpolator.getStartFrame(i));
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        for (int i = 0; i < FRAME_NUMBER_END; i++) {
            float width = mTextPaint.measureText(mFrameInterpolator.getEndFrame(i));
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        for (int i = 0; i < FRAME_NUMBER_MIDDLE; i++) {
            float width = mTextPaint.measureText(mFrameInterpolator.getMiddleFrame(i * 1.0f / FRAME_NUMBER_MIDDLE));
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    public void setFrameInterpolator(FrameInterpolator mFrameInterpolator) {
        this.mFrameInterpolator = mFrameInterpolator;
        
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawInit(canvas);
    }

    private void drawInit(Canvas canvas) {


        float x = (float) (getWidth() / 2.0);
        float y = (float) (getHeight() / 2.0);
        Paint.FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
        float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));


       /* mTextPaint.setMaskFilter(null);

        drawRect.left = 0;
        drawRect.top = (getHeight() - getWidth()) / 2;
        drawRect.right = getWidth();
        drawRect.bottom = drawRect.top + getWidth();
        canvas.drawRect(drawRect, testPaint);*/
        
        
        
        canvas.drawText(mFrameInterpolator.getStartFrame(0), x, baseline + scrollY, mTextPaint);
        canvas.drawText(mFrameInterpolator.getStartFrame(1), x, baseline +  mFrameOffset + scrollY, mFirstFramePaint);
        canvas.drawText(mFrameInterpolator.getStartFrame(2), x, baseline + mFrameOffset * 2 + scrollY, mSecondFramePaint);


        for (int i = 0; i < FRAME_NUMBER_MIDDLE; i++) {
            canvas.drawText(mFrameInterpolator.getMiddleFrame(i * 1.0f / FRAME_NUMBER_MIDDLE), x, baseline + mFrameOffset * (3 + i) + scrollY, mMiddleFramePaint);
        }

        canvas.drawText(mFrameInterpolator.getEndFrame(0), x, baseline + mFrameOffset * (FRAME_NUMBER_MIDDLE + 3) + scrollY, mSecondFramePaint);
        canvas.drawText(mFrameInterpolator.getEndFrame(1), x, baseline + mFrameOffset * (FRAME_NUMBER_MIDDLE + 4) + scrollY, mFirstFramePaint);
        canvas.drawText(mFrameInterpolator.getEndFrame(2), x, baseline + mFrameOffset * (FRAME_NUMBER_MIDDLE + 5) + scrollY, mTextPaint);

    }

    public void startAnim() {

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(this, "scrollY", 0, -mFrameOffset * 8);
        anim2.setInterpolator(new AccelerateInterpolator(5));
        anim2.setDuration(500);

        ObjectAnimator anim3 = ObjectAnimator.ofFloat(this, "scrollY", -mFrameOffset * 8, -mFrameOffset * (FRAME_NUMBER_MIDDLE + FRAME_NUMBER_START + FRAME_NUMBER_END - 1));
        anim3.setInterpolator(new OvershootInterpolator(0.45f));
        anim3.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(anim2, anim3);
        animatorSet.start();

    }


    public void setScrollY(float scrollY) {
        this.scrollY = scrollY;
        invalidate();
    }


    private void printLog(String log) {
        Log.e(TAG, String.format("---->  %s", log));
    }


    public void setFrameOffset(float mFrameOffset) {
        this.mFrameOffset = mFrameOffset;
    }

    
    public void setTextSize(float size) {
        if (size != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(size);

            mFirstFramePaint.setTextSize(size);
            mSecondFramePaint.setTextSize(size);
            mMiddleFramePaint.setTextSize(size);

            requestLayout();
            invalidate();
        }
    }

    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);

            mFirstFramePaint.setTypeface(tf);
            mSecondFramePaint.setTypeface(tf);
            mMiddleFramePaint.setTypeface(tf);
            
            requestLayout();
            invalidate();
        }
    }

    public void setTextColor(int color) {
        if (mTextPaint.getColor() != color) {
            mTextPaint.setColor(color);

            mFirstFramePaint.setColor(color);
            mSecondFramePaint.setColor(color);
            mMiddleFramePaint.setColor(color);

            invalidate();
        }
    }
    
}
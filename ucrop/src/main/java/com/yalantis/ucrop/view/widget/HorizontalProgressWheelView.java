package com.yalantis.ucrop.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.yalantis.ucrop.R;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
public class HorizontalProgressWheelView extends View {

    private final Rect mCanvasClipBounds = new Rect();

    private ScrollingListener mScrollingListener;
    private float mLastTouchedPosition;

    private Paint mProgressLinePaint;
    private Paint mProgressMiddleLinePaint;
    private int mProgressLineWidth, mProgressLineHeight, mProgressLineMidHeight;
    private int mProgressLineMargin;

    private boolean mScrollStarted;
    private float mTotalScrollDistance;

    private int mMiddleLineColor;

    public HorizontalProgressWheelView(Context context) {
        this(context, null);
    }

    public HorizontalProgressWheelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalProgressWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HorizontalProgressWheelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setScrollingListener(ScrollingListener scrollingListener) {
        mScrollingListener = scrollingListener;
    }

    public void setMiddleLineColor(@ColorInt int middleLineColor) {
        mMiddleLineColor = middleLineColor;
        mProgressMiddleLinePaint.setColor(mMiddleLineColor);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastTouchedPosition = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollingListener != null) {
                    mScrollStarted = false;
                    mScrollingListener.onScrollEnd();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getX() - mLastTouchedPosition;
                if (distance != 0) {
                    if (!mScrollStarted) {
                        mScrollStarted = true;
                        if (mScrollingListener != null) {
                            mScrollingListener.onScrollStart();
                        }
                    }
                    onScrollEvent(event, distance);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(mCanvasClipBounds);

        int linesCount = mCanvasClipBounds.width() / (mProgressLineWidth + mProgressLineMargin);
        float deltaX = (mTotalScrollDistance) % (float) (mProgressLineMargin + mProgressLineWidth);

        int temp = 0;
        mProgressLineHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_height_horizontal_wheel_progress_line);

        if(linesCount%2==0)
            linesCount = linesCount-1;
        for (int i = 1; i <= linesCount; i++) {
            if(i==(linesCount/2)+1){
                temp = 0;
            }
            else if(i<=(linesCount/2)){
                temp=i;
            }
            else if(i>=(linesCount/2)+1){
                temp = (linesCount/2)+1-i;
            }
            mProgressLineHeight=(mProgressLineHeight+temp);
            if (i < (linesCount / 3)) {
                mProgressLinePaint.setAlpha((int) (255 * (i / (float) (linesCount / 3))));

            } else if (i > (linesCount * 2 / 3)) {
                mProgressLinePaint.setAlpha((int) (255 * ((linesCount - i) / (float) (linesCount / 3))));
            } else {
                mProgressLinePaint.setAlpha(255);
            }
            canvas.drawCircle(-deltaX + mCanvasClipBounds.left + i * (mProgressLineWidth + mProgressLineMargin),
                    mCanvasClipBounds.centerY(),
                    4.5f,mProgressLinePaint);
//            canvas.drawLine(
//                    (-deltaX + mCanvasClipBounds.left + i * (mProgressLineWidth + mProgressLineMargin)),
//                    mCanvasClipBounds.centerY() - mProgressLineHeight,
//                    (-deltaX + mCanvasClipBounds.left + i * (mProgressLineWidth + mProgressLineMargin)),
//                    mCanvasClipBounds.centerY() + mProgressLineHeight, mProgressLinePaint);
        }
    }

    private void onScrollEvent(MotionEvent event, float distance) {
        mTotalScrollDistance -= distance;
        postInvalidate();
        mLastTouchedPosition = event.getX();
        if (mScrollingListener != null) {
            mScrollingListener.onScroll(-distance, mTotalScrollDistance);
        }
    }

    private void init() {
        mMiddleLineColor = ContextCompat.getColor(getContext(), R.color.ucrop_color_progress_wheel_line);
        mProgressLineWidth = getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_width_horizontal_wheel_progress_line);
        mProgressLineHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_height_horizontal_wheel_progress_line);
        mProgressLineMidHeight = getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_height_horizontal_wheel_progress_line_mid);
        mProgressLineMargin = getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_margin_horizontal_wheel_progress_line);

        mProgressLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressLinePaint.setStyle(Paint.Style.FILL);
        mProgressLinePaint.setStrokeWidth(mProgressLineWidth);
        mProgressLinePaint.setColor(getResources().getColor(R.color.ucrop_color_progress_wheel_line));

        mProgressMiddleLinePaint = new Paint(mProgressLinePaint);
        mProgressMiddleLinePaint.setColor(mMiddleLineColor);
        mProgressMiddleLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressMiddleLinePaint.setStrokeWidth(getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_width_middle_wheel_progress_line));
    }

    public interface ScrollingListener {

        void onScrollStart();

        void onScroll(float delta, float totalDistance);

        void onScrollEnd();
    }

}

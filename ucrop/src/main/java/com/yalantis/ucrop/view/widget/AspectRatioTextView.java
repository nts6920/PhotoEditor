package com.yalantis.ucrop.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;

import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
public class AspectRatioTextView extends AppCompatTextView {

    private final float MARGIN_MULTIPLIER = 3f;
    private final Rect mCanvasClipBounds = new Rect();
    private Paint mDotPaint;
    private int mDotSize;
    private float mAspectRatio;
    private String mAspectRatioTitle;
    private float mAspectRatioX, mAspectRatioY;

    public AspectRatioTextView(Context context) {
        this(context, null);
    }

    public AspectRatioTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRatioTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_AspectRatioTextView);
        init(a);
    }

    /**
     * @param activeColor the resolved color for active elements
     */

    public void setActiveColor(@ColorInt int activeColor) {
        applyActiveColor(activeColor);
        invalidate();
    }

    public void setAspectRatio(@NonNull AspectRatio aspectRatio) {
        mAspectRatioTitle = aspectRatio.getAspectRatioTitle();
        mAspectRatioX = aspectRatio.getAspectRatioX();
        mAspectRatioY = aspectRatio.getAspectRatioY();

        if (mAspectRatioX == CropImageView.SOURCE_IMAGE_ASPECT_RATIO || mAspectRatioY == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            mAspectRatio = CropImageView.SOURCE_IMAGE_ASPECT_RATIO;
        } else {
            mAspectRatio = mAspectRatioX / mAspectRatioY;
        }

        setTitle();
    }

    public float getAspectRatio(boolean toggleRatio) {
        if (toggleRatio) {
            toggleAspectRatio();
            setTitle();
        }
        return mAspectRatio;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.button_crop);
//        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap,getWidth(),getWidth(),false);
//        canvas.drawBitmap(bitmap1,new Matrix(),mDotPaint);
//        Rect bound = new Rect();
//        if((int)(mAspectRatioX)==0){
//            mDotPaint.measureText(mAspectRatioTitle);
//        }
//        else mDotPaint.measureText((int)mAspectRatioX+":"+(int)mAspectRatioY);
//        mDotPaint.measureText((int)mAspectRatioX+":"+(int)mAspectRatioY);
//        mDotPaint.getTextBounds((int)mAspectRatioX+":"+(int)mAspectRatioY,0,1,bound);
//        widthText = bound.width();
//        heightText = bound.height();
//        mDotPaint.setTextSize(15);
//        if((int)(mAspectRatioX)==0){
//            canvas.drawText(mAspectRatioTitle,getWidth()/2,getHeight()/2,mDotPaint);
//        }
//        else {
//            canvas.drawText((int) mAspectRatioX + ":" + (int) mAspectRatioY, (getWidth() - widthText) / 2, (getHeight() - heightText) / 2, mDotPaint);
//        }
//        if (isSelected()) {
//            canvas.getClipBounds(mCanvasClipBounds);
//
//            float x = (mCanvasClipBounds.right - mCanvasClipBounds.left) / 2.0f;
//            float y = (mCanvasClipBounds.bottom - mCanvasClipBounds.top / 2f) - mDotSize * MARGIN_MULTIPLIER;
//
////            canvas.drawCircle(x, y, mDotSize / 2f, mDotPaint);
//        }
    }

    @SuppressWarnings("deprecation")
    private void init(@NonNull TypedArray a) {
        setGravity(Gravity.CENTER);
        mAspectRatioTitle = a.getString(R.styleable.ucrop_AspectRatioTextView_ucrop_artv_ratio_title);
        mAspectRatioX = a.getFloat(R.styleable.ucrop_AspectRatioTextView_ucrop_artv_ratio_x, CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
        mAspectRatioY = a.getFloat(R.styleable.ucrop_AspectRatioTextView_ucrop_artv_ratio_y, CropImageView.SOURCE_IMAGE_ASPECT_RATIO);

        if (mAspectRatioX == CropImageView.SOURCE_IMAGE_ASPECT_RATIO || mAspectRatioY == CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            mAspectRatio = CropImageView.SOURCE_IMAGE_ASPECT_RATIO;
        } else {
            mAspectRatio = mAspectRatioX / mAspectRatioY;
        }

        mDotSize = getContext().getResources().getDimensionPixelSize(R.dimen.ucrop_size_dot_scale_text_view);
        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setStyle(Paint.Style.FILL);

        setTitle();
        int activeColor = getResources().getColor(R.color.ucrop_color_light_blue);
        applyActiveColor(activeColor);

        a.recycle();
    }

    private void applyActiveColor(@ColorInt int activeColor) {
        if (mDotPaint != null) {
            mDotPaint.setColor(activeColor);
        }
        ColorStateList textViewColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{0}
                },
                new int[]{
                        activeColor,
                        ContextCompat.getColor(getContext(), R.color.ucrop_color_white)
                }
        );

        setTextColor(textViewColorStateList);
    }

    private void toggleAspectRatio() {
        if (mAspectRatio != CropImageView.SOURCE_IMAGE_ASPECT_RATIO) {
            float tempRatioW = mAspectRatioX;
            mAspectRatioX = mAspectRatioY;
            mAspectRatioY = tempRatioW;

            mAspectRatio = mAspectRatioX / mAspectRatioY;
        }
    }

    private void setTitle() {
        setTextSize(10);
        if (!TextUtils.isEmpty(mAspectRatioTitle)) {
            setText(mAspectRatioTitle);
        } else {
            setText(String.format(Locale.US, "%d:%d", (int) mAspectRatioX, (int) mAspectRatioY));
        }
    }

}

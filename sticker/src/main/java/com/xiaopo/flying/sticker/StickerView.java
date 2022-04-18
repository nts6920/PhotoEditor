package com.xiaopo.flying.sticker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * sticker view
 * Created by snowbean on 16-8-2.
 */

public class StickerView extends androidx.appcompat.widget.AppCompatImageView {
    public StickerView addSticker(Sticker sticker) {
        return addSticker(sticker);
    }

    private enum ActionMode {
        NONE,   //nothing
        DRAG,   //drag the sticker with your finger
        ZOOM_WITH_TWO_FINGER,   //zoom in or zoom out the sticker and rotate the sticker with two finger
        ZOOM_WITH_ICON,    //zoom in or zoom out the sticker and rotate the sticker with icon
        DELETE,  //delete the handling sticker
        FLIP_HORIZONTAL, //horizontal flip the sticker
        CLICK    //Click the Sticker
    }

    private static final String TAG = "StickerView";
    public static final float DEFAULT_ICON_RADIUS = 30f;
    public static final float DEFAULT_ICON_EXTRA_RADIUS = 10f;
    private int progress;
    private Paint mBorderPaint;
    private Paint cornerPaint;
    private Paint paintBg;
    private Paint painDelete;
    private Boolean isInEdit = true;
    private RectF mStickerRect;
    private Matrix mSizeMatrix;
    private Matrix mDownMatrix;
    private Matrix mMoveMatrix;
    private boolean setMove = true;
    private BitmapStickerIcon mDeleteIcon;
    private BitmapStickerIcon mZoomIcon1,mZoomIcon4,mZoomIcon3;


    private float mIconRadius = DEFAULT_ICON_RADIUS;
    private float mIconExtraRadius = DEFAULT_ICON_EXTRA_RADIUS;

    //the first point down position
    private float mDownX;
    private float mDownY;
    private final float pointerLimitDis = 20f;
    private final float pointerZoomCoeff = 0.09f;
    private float mOldDistance = 1f;
    private float mOldRotation = 0;
    private PointF mMidPoint;

    private ActionMode mCurrentMode = ActionMode.NONE;
    public List<Sticker> redoStickerStates = new ArrayList<>();
    public List<TextStickerState> textStickerStates = new ArrayList<>();
    public List<TextStickerState> redoTextStickerStates = new ArrayList<>();
    public List<Sticker> mStickers = new ArrayList<>();
    private Sticker mHandlingSticker;
    private int countSticker = 0;
    private int alpha = 255;
    private String color = "";
    private String path = "";
    private boolean mLooked;

    private int mTouchSlop;

    // undo
    public List<Sticker> undoStickerStates = new ArrayList<>();

//    private boolean locked;

    private OnStickerClickListener mOnStickerClickListener;

    public StickerView(Context context) {
        this(context, null);
    }

    public StickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        cornerPaint = new Paint();
        mBorderPaint = new Paint();
        painDelete = new Paint();
        painDelete.setAntiAlias(true);
        paintBg = new Paint();
        paintBg.setAntiAlias(true);
        setColorBg(getContext().getResources().getColor(R.color.colorBLack));
        painDelete.setColor(context.getResources().getColor(R.color.colorWhite));
        cornerPaint.setAntiAlias(true);
        cornerPaint.setColor(context.getResources().getColor(R.color.color_ic_sticker));
        cornerPaint.setStrokeWidth(15);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(context.getResources().getColor(R.color.color_ic_sticker));
        mSizeMatrix = new Matrix();
        mDownMatrix = new Matrix();
        mMoveMatrix = new Matrix();
        mStickerRect = new RectF();
        mDeleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_delete));
        mZoomIcon1 = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.mipmap.ic_vt1));
        mZoomIcon3 = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.mipmap.ic_vt3));
        mZoomIcon4 = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.mipmap.ic_vt2));
//        setupDraw();
    }

    public void setColorBg(int color) {
        paintBg.setColor(color);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mStickerRect.left = left;
            mStickerRect.top = top;
            mStickerRect.right = right;
            mStickerRect.bottom = bottom;
        }
    }

    public void setColor(int color) {
        if (mHandlingSticker != null) {
            Bitmap bb = ((BitmapDrawable) ((DrawableSticker) mHandlingSticker).getDrawable()).getBitmap();
            if (bb != null) {
                Bitmap bm = Bitmap.createBitmap(bb.getWidth(), bb.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bm);
                canvas.drawColor(color);
                Bitmap bitmapResult = Bitmap.createBitmap(bb.getWidth(), bb.getHeight(), Bitmap.Config.ARGB_8888);
                Paint paint = new Paint();
                Canvas canvas1 = new Canvas(bitmapResult);
                canvas1.drawBitmap(bb, 0, 0, null);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas1.drawBitmap(bm, 0, 0, paint);
//        setBitmap(bitmapResult);
                replace(bitmapResult);
                invalidate();
            }
        }
    }

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mStickers.size(); i++) {
            Sticker sticker = mStickers.get(i);
            if (sticker != null) {
                sticker.draw(canvas);
            }
        }

        if (mHandlingSticker != null && !mLooked) {

            float[] bitmapPoints = getStickerPoints(mHandlingSticker);
            float x1 = bitmapPoints[0];
            float y1 = bitmapPoints[1];
            float x2 = bitmapPoints[2];
            float y2 = bitmapPoints[3];
            float x3 = bitmapPoints[4];
            float y3 = bitmapPoints[5];
            float x4 = bitmapPoints[6];
            float y4 = bitmapPoints[7];

            float[] cornerPoints = getStickerCornerPoints(mHandlingSticker);

            //draw 4 sides
            if (isInEdit) {
                mBorderPaint.setPathEffect(new DashPathEffect(new float[]{x2 / 8, 16}, 0));
                canvas.drawLine(x1, y1, x2, y2, mBorderPaint);
                canvas.drawLine(x1, y1, x3, y3, mBorderPaint);
                canvas.drawLine(x2, y2, x4, y4, mBorderPaint);
                canvas.drawLine(x4, y4, x3, y3, mBorderPaint);

                //draw 4 corner

                float rotation = calculateRotation(x3, y3, x4, y4);
                float rotation1 = calculateRotation(x1, y1, x2, y2);
                //draw delete icon
                canvas.drawCircle(x2, y2, mIconRadius, painDelete);
                mDeleteIcon.setX(x2);
                mDeleteIcon.setY(y2);
                mDeleteIcon.getMatrix().reset();

                mDeleteIcon.getMatrix().postRotate(
                        0, mDeleteIcon.getWidth() / 2, mDeleteIcon.getHeight() / 2);
                mDeleteIcon.getMatrix().postTranslate(
                        x2 - mDeleteIcon.getWidth() / 2, y2 - mDeleteIcon.getHeight() / 2);

                mDeleteIcon.draw(canvas);

                mZoomIcon4.setX(x4);
                mZoomIcon4.setY(y4);
                mZoomIcon4.getMatrix().reset();
                mZoomIcon4.getMatrix().postRotate(
                        180f + rotation, mZoomIcon4.getWidth() / 1.2f,
                        mZoomIcon4.getHeight() / 1.2f);

                mZoomIcon4.getMatrix().postTranslate(
                        x4 - mZoomIcon4.getWidth() / 1.2f,
                        y4 - mZoomIcon4.getHeight() / 1.2f);
                mZoomIcon4.draw(canvas);

                mZoomIcon1.setX(x1);
                mZoomIcon1.setY(y1);
                mZoomIcon1.getMatrix().reset();
                mZoomIcon1.getMatrix().postRotate(
                        180f + rotation1, mZoomIcon1.getWidth() / 6f,
                        mZoomIcon1.getHeight() / 6f);

                mZoomIcon1.getMatrix().postTranslate(
                        x1 - mZoomIcon1.getWidth() / 6f,
                        y1 - mZoomIcon1.getHeight() / 6f);
                mZoomIcon1.draw(canvas);

                mZoomIcon3.setX(x3);
                mZoomIcon3.setY(y3);

                mZoomIcon3.getMatrix().reset();
                mZoomIcon3.getMatrix().postRotate(
                        180f +rotation, mZoomIcon3.getWidth() / 6f, mZoomIcon3.getHeight() / 1.2f);
                mZoomIcon3.getMatrix().postTranslate(
                        x3 - mZoomIcon3.getWidth() / 6f, y3 - mZoomIcon3.getHeight() / 1.2f);
                mZoomIcon3.draw(canvas);
            }
        }

    }

    public void setInEdit(boolean isInEdit) {
        this.isInEdit = isInEdit;
        invalidate();
    }

    public Boolean getInEdit() {
        return isInEdit;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLooked) return super.onTouchEvent(event);

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCurrentMode = ActionMode.DRAG;
                setInEdit(true);
                mDownX = event.getX();
                mDownY = event.getY();
                if (checkDeleteIconTouched(mIconExtraRadius)) {
                    mCurrentMode = ActionMode.DELETE;
                } else if (checkZoomIconTouched(mIconExtraRadius) && mHandlingSticker != null) {
                    mCurrentMode = ActionMode.ZOOM_WITH_ICON;
                    mMidPoint = calculateMidPoint();
                    mOldDistance = calculateDistance(mMidPoint.x, mMidPoint.y, mDownX, mDownY);
                    mOldRotation = calculateRotation(mMidPoint.x, mMidPoint.y, mDownX, mDownY);
                } else {
                    mHandlingSticker = findHandlingSticker();
                }

                if (mHandlingSticker != null) {
                    mDownMatrix.set(mHandlingSticker.getMatrix());
                }
                if (mOnStickerClickListener != null) {
                    mOnStickerClickListener.onStickerClick(mHandlingSticker);
                }
                invalidate();
                break;


            case MotionEvent.ACTION_POINTER_DOWN:
                setInEdit(true);
                if (spacing(event) > pointerLimitDis) {
                    mOldDistance = calculateDistance(event);
                    mOldRotation = calculateRotation(event);

                    mMidPoint = calculateMidPoint(event);

                    if (mHandlingSticker != null &&
                            isInStickerArea(mHandlingSticker, event.getX(1), event.getY(1)) &&
                            !checkDeleteIconTouched(mIconExtraRadius))

                        mCurrentMode = ActionMode.ZOOM_WITH_TWO_FINGER;
                    break;
                }


            case MotionEvent.ACTION_MOVE:
                if (setMove) {
                    handleCurrentMode(event);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mCurrentMode == ActionMode.DELETE && mHandlingSticker != null) {
                    for (int i = 0; i < mStickers.size(); i++) {
                        if (mHandlingSticker.equals(mStickers.get(i))) {
                            undoStickerStates.add(mStickers.get(i));
                            mStickers.remove(i);
                        }
                    }
                    mHandlingSticker = null;
                    invalidate();

//                    deleteSticker();
                }

                if (mCurrentMode == ActionMode.FLIP_HORIZONTAL && mHandlingSticker != null) {
                    mHandlingSticker.getMatrix().preScale(-1, 1,
                            mHandlingSticker.getCenterPoint().x, mHandlingSticker.getCenterPoint().y);

                    mHandlingSticker.setFlipped(!mHandlingSticker.isFlipped());
                    invalidate();
                }

                if (mCurrentMode == ActionMode.DRAG
                        && Math.abs(event.getX() - mDownX) < mTouchSlop
                        && Math.abs(event.getY() - mDownY) < mTouchSlop
                        && mHandlingSticker != null) {
                    mCurrentMode = ActionMode.CLICK;
//                    if (mOnStickerClickListener != null) {
//                        mOnStickerClickListener.onStickerClick(mHandlingSticker);
//                    }
                }

                mCurrentMode = ActionMode.NONE;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mCurrentMode = ActionMode.NONE;
                break;

        }//end of switch(action)

        return true;
    }

    public void undoSticker() {
        if (mStickers.size() > 0) {
            undoStickerStates.add(mStickers.get(mStickers.size() - 1));
            mStickers.remove(mStickers.get(mStickers.size() - 1));
            invalidate();
        }
    }

    public void redoSticker() {
        if (undoStickerStates.size() > 0) {
            mStickers.add(undoStickerStates.get(undoStickerStates.size() - 1));
            undoStickerStates.remove(undoStickerStates.get(undoStickerStates.size() - 1));
            invalidate();
        }
    }

//    public void deleteSticker() {
//        if (mHandlingSticker != null) {
//            mStickers.remove(mHandlingSticker);
//            mHandlingSticker = null;
//            undoStickerStates.clear();
//            invalidate();
//        }
//    }

    public void deleteSticker() {
        if (mHandlingSticker != null) {
            mStickers.remove(mHandlingSticker);
            mHandlingSticker.release();
            mHandlingSticker = null;
            invalidate();
        }

    }

    public void removeAllSticker() {
        mStickers.clear();
        if (mHandlingSticker != null) {
            mHandlingSticker.release();
            mHandlingSticker = null;
        }
        invalidate();
    }

    private void handleCurrentMode(MotionEvent event) {
        switch (mCurrentMode) {
            case NONE:
                break;
            case DRAG:

                if (mHandlingSticker != null) {
                    mMoveMatrix.set(mDownMatrix);
                    float dx = event.getX() - mDownX;
                    float dy = event.getY() - mDownY;
                    if (dx >= -1 && dx <= 1 || dy >= -1 && dy <= 1) {
                        return;
                    } else {

                        mMoveMatrix.postTranslate(dx, dy);
//                        mHandlingSticker.getMatrix().set(mMoveMatrix);
                        mHandlingSticker.setMatrix(mMoveMatrix);
                    }
                }
                break;
            case ZOOM_WITH_TWO_FINGER:
                if (mHandlingSticker != null) {
                    float newDistance = calculateDistance(event);
                    float newRotation = calculateRotation(event);

                    mMoveMatrix.set(mDownMatrix);
                    mMoveMatrix.postScale(
                            newDistance / mOldDistance, newDistance / mOldDistance, mMidPoint.x, mMidPoint.y);
                    mMoveMatrix.postRotate(newRotation - mOldRotation, mMidPoint.x, mMidPoint.y);
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//                    mHandlingSticker.getMatrix().set(mMoveMatrix);
                    mHandlingSticker.setMatrix(mMoveMatrix);
                }

                break;

            case ZOOM_WITH_ICON:
                if (mHandlingSticker != null) {
                    float newDistance = calculateDistance(mMidPoint.x, mMidPoint.y, event.getX(), event.getY());
                    float newRotation = calculateRotation(mMidPoint.x, mMidPoint.y, event.getX(), event.getY());
                    mMoveMatrix.set(mDownMatrix);
                    mMoveMatrix.postScale(
                            newDistance / mOldDistance, newDistance / mOldDistance, mMidPoint.x, mMidPoint.y);
                    mMoveMatrix.postRotate(newRotation - mOldRotation, mMidPoint.x, mMidPoint.y);
//                    mHandlingSticker.getMatrix().set(mMoveMatrix);
                    mHandlingSticker.setMatrix(mMoveMatrix);
                }

                break;
        }// end of switch(mCurrentMode)
    }

    //判断是否按在缩放按钮区域
    private boolean checkZoomIconTouched(float extraRadius) {
        float x = mZoomIcon4.getX() - mDownX;
        float y = mZoomIcon4.getY() - mDownY;
        float distance_pow_2 = x * x + y * y;
        return distance_pow_2 <= (mIconRadius + extraRadius) * (mIconRadius + extraRadius);
    }

    //判断是否按在删除按钮区域
    private boolean checkDeleteIconTouched(float extraRadius) {
        float x = mDeleteIcon.getX() - mDownX;
        float y = mDeleteIcon.getY() - mDownY;
        float distance_pow_2 = x * x + y * y;
        return distance_pow_2 <= (mIconRadius + extraRadius) * (mIconRadius + extraRadius);
    }

    //找到点击的区域属于哪个贴纸
    private Sticker findHandlingSticker() {
        for (int i = mStickers.size() - 1; i >= 0; i--) {
            if (isInStickerArea(mStickers.get(i), mDownX, mDownY)) {
                return mStickers.get(i);
            }
        }
        return null;
    }

    private boolean isInStickerArea(Sticker sticker, float downX, float downY) {
        RectF dst = sticker.getMappedBound();
        return dst.contains(downX, downY);
    }

    private PointF calculateMidPoint(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) return new PointF();
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    private PointF calculateMidPoint() {
        if (mHandlingSticker == null) return new PointF();
        return mHandlingSticker.getMappedCenterPoint();
    }

    //计算两点形成的直线与x轴的旋转角度
    private float calculateRotation(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) return 0f;
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);
        double radians = Math.atan2(y, x);
        return (float) Math.toDegrees(radians);
    }

    private float calculateRotation(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        double radians = Math.atan2(y, x);
        return (float) Math.toDegrees(radians);
    }

    //计算两点间的距离
    private float calculateDistance(MotionEvent event) {
        if (event == null || event.getPointerCount() < 2) return 0f;
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        double x = x1 - x2;
        double y = y1 - y2;

        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (int i = 0; i < mStickers.size(); i++) {
            Sticker sticker = mStickers.get(i);
            if (sticker != null) {
                transformSticker(sticker, w, h, oldw, oldh);
            }
        }

        invalidate();
    }

    //sticker的图片会过大或过小，需要转化
    //step 1：使sticker图片的中心与View的中心重合
    //step 2：计算缩放值，进行缩放
//    protected void transformSticker(Sticker sticker) {
//        if (sticker == null) {
//            Log.e(TAG, "transformSticker: the bitmapSticker is null or the bitmapSticker bitmap is null");
//            return;
//        }
//
//
//        if (mSizeMatrix != null) {
//            mSizeMatrix.reset();
//        }
//
//        //step 1
//        float offsetX = (getWidth() - sticker.getWidth()) / 2;
//        float offsetY = (getHeight() - sticker.getHeight()) / 2;
//
//        mSizeMatrix.postTranslate(offsetX, offsetY);
//
//        //step 2
//        float scaleFactor;
//        if (getWidth() < getHeight()) {
//            scaleFactor = (float) getWidth() / sticker.getWidth();
//        } else {
//            scaleFactor = (float) getHeight() / sticker.getHeight();
//        }
//
//        mSizeMatrix.postScale(scaleFactor / 2, scaleFactor / 2,
//                getWidth() / 2, getHeight() / 2);
//
//        sticker.getMatrix().reset();
////        sticker.getMatrix().set(mSizeMatrix);
//        sticker.setMatrix(mSizeMatrix);
//
//        invalidate();
//    }

    protected void transformSticker(Sticker sticker, int w, int h, int oldW, int oldH) {
        mSizeMatrix.set(sticker.getMatrix());
        float stickerWidth = sticker.getCurrentWidth();
        float stickerHeight = sticker.getCurrentHeight();
        float translateX = sticker.getMatrixValue(mSizeMatrix, Matrix.MTRANS_X);
        float translateY = sticker.getMatrixValue(mSizeMatrix, Matrix.MTRANS_Y);
        float scaleX = (float) w / oldW;
        float scaleY = (float) h / oldH;
        // move the origin to center of the sticker to make scaling easier
        mSizeMatrix.postTranslate(-translateX - stickerWidth / 2f, -translateY - stickerHeight / 2f);
        // scale the sticker according to the difference in width and height
        mSizeMatrix.postScale(scaleX, scaleY);
        // move the origin to where it was, but also apply the scale to the coordinates
        mSizeMatrix.postTranslate((translateX + stickerWidth / 2f) * scaleX,
                (translateY + stickerHeight / 2f) * scaleY);
        sticker.setMatrix(mSizeMatrix);
    }

    public void replace(Drawable stickerDrawable) {
        if (mHandlingSticker != null) {
            if (mHandlingSticker instanceof DrawableSticker) {
                ((DrawableSticker) mHandlingSticker).setDrawable(stickerDrawable);
            } else {
                Log.d(TAG, "replace: the current sticker did not support drawable");
            }

            invalidate();
        }
    }

    public void replace(Bitmap stickerBitmap) {
        replace(new BitmapDrawable(getResources(), stickerBitmap));
    }

    public void replace(Sticker sticker, boolean needStayState) {
        if (mHandlingSticker != null && sticker != null) {
            if (needStayState) {
//                sticker.getMatrix().set(mHandlingSticker.getMatrix());
                sticker.setMatrix(mHandlingSticker.getMatrix()); //
                sticker.setFlipped(mHandlingSticker.isFlipped());
            }
            int index = mStickers.indexOf(mHandlingSticker);
            mStickers.set(index, sticker);
            mHandlingSticker = sticker;
            invalidate();
        }
    }

    //更
    public float[] getStickerPoints(Sticker sticker) {
        if (sticker == null) return new float[8];

        return sticker.getMappedBoundPoints();
    }

    public float[] getStickerCornerPoints(Sticker sticker) {
        if (sticker == null) return new float[24];
        return sticker.getMappedCornerPoints();
    }


    public void addSticker(Bitmap stickerBitmap) {
        addSticker(new BitmapDrawable(getResources(), stickerBitmap));
    }

    public void addSticker(Drawable stickerDrawable) {
        Sticker drawableSticker = new DrawableSticker(stickerDrawable);

        float offsetX = (getWidth() - drawableSticker.getWidth()) / 2;
        float offsetY = (getHeight() - drawableSticker.getHeight()) / 2;
        drawableSticker.getMatrix().postTranslate(offsetX, offsetY);

        float scaleFactor;
        if (getWidth() < getHeight()) {
            scaleFactor = (float) getWidth() / stickerDrawable.getIntrinsicWidth();
        } else {
            scaleFactor = (float) getHeight() / stickerDrawable.getIntrinsicWidth();
        }
        drawableSticker.getMatrix().postScale(scaleFactor / 2, scaleFactor / 2, getWidth() / 2, getHeight() / 2);

        mHandlingSticker = drawableSticker;
        mStickers.add(drawableSticker);
        invalidate();
    }

    public void save(File file) {
        Bitmap bitmap = null;
        FileOutputStream outputStream = null;

        try {
            bitmap = createBitmap();
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            BitmapUtil.notifySystemGallery(getContext(), file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public Bitmap createBitmap() {
        mHandlingSticker = null;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        return bitmap;
    }

    public float getIconRadius() {
        return mIconRadius;
    }

    public void setIconRadius(float iconRadius) {
        mIconRadius = iconRadius;
        invalidate();
    }

    public float getIconExtraRadius() {
        return mIconExtraRadius;
    }

    public void setIconExtraRadius(float iconExtraRadius) {
        mIconExtraRadius = iconExtraRadius;
    }

    public boolean isLooked() {
        return mLooked;
    }

    public void setLooked(boolean looked) {
        mLooked = looked;
        invalidate();
    }

    public void setOnStickerClickListener(OnStickerClickListener onStickerClickListener) {
        mOnStickerClickListener = onStickerClickListener;
    }

    public interface OnStickerClickListener {
        void onStickerClick(Sticker sticker);
    }

    public Sticker getSticker() {
        return mHandlingSticker;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}

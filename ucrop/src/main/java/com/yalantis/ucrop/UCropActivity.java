package com.yalantis.ucrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;

import com.yalantis.ucrop.adapter.RatioAdapter;
import com.yalantis.ucrop.callback.BitmapCropCallback;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.model.ViewState;
import com.yalantis.ucrop.util.RatioUtils;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@SuppressWarnings("ConstantConditions")
public class UCropActivity extends AppCompatActivity implements RatioAdapter.OnClickRatio {
    private Uri inputUri, outputUri;
    public static final int DEFAULT_COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    public static final int NONE = 0;
    public static final int SCALE = 1;
    public static final int ROTATE = 2;
    public static final int ALL = 3;

    @IntDef({NONE, SCALE, ROTATE, ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GestureTypes {
    }

    private static final long CONTROLS_ANIMATION_DURATION = 50;
    private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;

    @ColorInt
    private int mRootViewBackgroundColor;
    private RatioAdapter ratioAdapter;
    private boolean mShowBottomControls;
    private UCropView mUCropView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private TextView mTextViewRotateAngle;
    private View mBlockingView;
    private ViewState viewState;
    private List<ViewState> listState = new ArrayList<>();
    private List<ViewState> redoList = new ArrayList<>();
    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ucrop_activity_photobox);

        final Intent intent = getIntent();
        setupViews(intent);
        setImageData(intent);
        addBlockingView();
        setStatusbar();
        setupSaveImage();
        cancelCrop();
        undoCrop();
        redoCrop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setStatusbar() {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.ucrop_back_ground));

    }

    @Override
    protected void onResume() {
        super.onResume();
        inputUri = null;
        outputUri = null;

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGestureCropImageView != null) {
            mGestureCropImageView.cancelAllAnimations();
            inputUri = null;
            outputUri = null;
            mGestureCropImageView = null;
        }
    }

    /**
     * This method extracts all data from the incoming intent and setups views properly.
     */
    private void setImageData(@NonNull Intent intent) {
        inputUri = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI);
        outputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
        processOptions(intent);

        if (inputUri != null && outputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri, outputUri);
            } catch (Exception e) {
                setResultError(e);
                finish();
            }
        } else {
            setResultError(new NullPointerException(getString(R.string.ucrop_error_input_data_is_absent)));
            finish();
        }
    }

    /**
     * This method extracts {@link com.yalantis.ucrop.UCrop.Options #optionsBundle} from incoming intent
     * and setups Activity, {@link OverlayView} and {@link CropImageView} properly.
     */
    @SuppressWarnings("deprecation")
    private void processOptions(@NonNull Intent intent) {
        // Bitmap compression options
        String compressionFormatName = intent.getStringExtra(UCrop.Options.EXTRA_COMPRESSION_FORMAT_NAME);
        Bitmap.CompressFormat compressFormat = null;
        if (!TextUtils.isEmpty(compressionFormatName)) {
            compressFormat = Bitmap.CompressFormat.valueOf(compressionFormatName);
        }
        mCompressFormat = (compressFormat == null) ? DEFAULT_COMPRESS_FORMAT : compressFormat;

        mCompressQuality = intent.getIntExtra(UCrop.Options.EXTRA_COMPRESSION_QUALITY, UCropActivity.DEFAULT_COMPRESS_QUALITY);

        // Crop image view options
        mGestureCropImageView.setMaxBitmapSize(intent.getIntExtra(UCrop.Options.EXTRA_MAX_BITMAP_SIZE, CropImageView.DEFAULT_MAX_BITMAP_SIZE));
        mGestureCropImageView.setMaxScaleMultiplier(intent.getFloatExtra(UCrop.Options.EXTRA_MAX_SCALE_MULTIPLIER, CropImageView.DEFAULT_MAX_SCALE_MULTIPLIER));
        mGestureCropImageView.setImageToWrapCropBoundsAnimDuration(intent.getIntExtra(UCrop.Options.EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, CropImageView.DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION));

        // Overlay view options
        mOverlayView.setFreestyleCropEnabled(intent.getBooleanExtra(UCrop.Options.EXTRA_FREE_STYLE_CROP, OverlayView.DEFAULT_FREESTYLE_CROP_MODE != OverlayView.FREESTYLE_CROP_MODE_DISABLE));

        mOverlayView.setDimmedColor(intent.getIntExtra(UCrop.Options.EXTRA_DIMMED_LAYER_COLOR, getResources().getColor(R.color.ucrop_color_default_dimmed)));
        mOverlayView.setCircleDimmedLayer(intent.getBooleanExtra(UCrop.Options.EXTRA_CIRCLE_DIMMED_LAYER, OverlayView.DEFAULT_CIRCLE_DIMMED_LAYER));

        mOverlayView.setShowCropFrame(intent.getBooleanExtra(UCrop.Options.EXTRA_SHOW_CROP_FRAME, OverlayView.DEFAULT_SHOW_CROP_FRAME));
        mOverlayView.setCropFrameColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_frame)));
        mOverlayView.setCropFrameStrokeWidth(intent.getIntExtra(UCrop.Options.EXTRA_CROP_FRAME_STROKE_WIDTH, getResources().getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width)));

        mOverlayView.setCropGridColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_frame)));
        mOverlayView.setCropGridCornerColor(intent.getIntExtra(UCrop.Options.EXTRA_CROP_GRID_CORNER_COLOR, getResources().getColor(R.color.ucrop_color_default_crop_grid)));
        mOverlayView.setCropGridStrokeWidth(14);

        // Aspect ratio options
        float aspectRatioX = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, 0);
        float aspectRatioY = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 0);

        int aspectRationSelectedByDefault = intent.getIntExtra(UCrop.Options.EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, 0);
        ArrayList<AspectRatio> aspectRatioList = intent.getParcelableArrayListExtra(UCrop.Options.EXTRA_ASPECT_RATIO_OPTIONS);

        if (aspectRatioX > 0 && aspectRatioY > 0) {
            mGestureCropImageView.setTargetAspectRatio(aspectRatioX / aspectRatioY);
        } else if (aspectRatioList != null && aspectRationSelectedByDefault < aspectRatioList.size()) {
            mGestureCropImageView.setTargetAspectRatio(aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioX() /
                    aspectRatioList.get(aspectRationSelectedByDefault).getAspectRatioY());
        } else {
            mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
        }

        // Result bitmap max size options
        int maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0);
        int maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0);

        if (maxSizeX > 0 && maxSizeY > 0) {
            mGestureCropImageView.setMaxResultImageSizeX(maxSizeX);
            mGestureCropImageView.setMaxResultImageSizeY(maxSizeY);
        }
    }

    private void setupViews(@NonNull Intent intent) {
        viewState = new ViewState();
        listState.add(viewState);
        mShowBottomControls = !intent.getBooleanExtra(UCrop.Options.EXTRA_HIDE_BOTTOM_CONTROLS, false);
        mRootViewBackgroundColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_crop_background));
        initiateRootViews();
        ViewGroup viewGroup = findViewById(R.id.ucrop_photobox);
        ViewGroup wrapper = viewGroup.findViewById(R.id.controls_wrapper);
        wrapper.setVisibility(View.VISIBLE);
        LayoutInflater.from(this).inflate(R.layout.ucrop_controls, wrapper, true);
        Transition mControlsTransition = new AutoTransition();
        mControlsTransition.setDuration(CONTROLS_ANIMATION_DURATION);
        setupAspectRatioWidget();
        setupRotateWidget();
    }

    private void initiateRootViews() {
        mUCropView = findViewById(R.id.ucrop);
        mGestureCropImageView = mUCropView.getCropImageView();
        mOverlayView = mUCropView.getOverlayView();

        mGestureCropImageView.setTransformImageListener(mImageListener);

        findViewById(R.id.ucrop_frame).setBackgroundColor(mRootViewBackgroundColor);
        if (!mShowBottomControls) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.ucrop_frame).getLayoutParams();
            params.bottomMargin = 0;
            findViewById(R.id.ucrop_frame).requestLayout();
        }
    }

    private final TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {

        @Override
        public void onRotate(float currentAngle) {
            setAngleText(currentAngle);
        }

        @Override
        public void onScale(float currentScale) {
        }

        @Override
        public void onLoadComplete() {
            mUCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
            mBlockingView.setClickable(false);
            supportInvalidateOptionsMenu();
        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
            setResultError(e);
            finish();
        }

    };

    private void setupAspectRatioWidget() {
        final ArrayList<AspectRatio> aspectRatioList = RatioUtils.getRatioList(getApplicationContext());
        final RecyclerView rvRatio = findViewById(R.id.layout_aspect_ratio);
        ratioAdapter = new RatioAdapter(aspectRatioList, getBaseContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvRatio.setLayoutManager(layoutManager);
        rvRatio.setAdapter(ratioAdapter);
    }

    @Override
    public void onChangeRatio(float ratio, int id) {
        viewState = new ViewState();
        mGestureCropImageView.setTargetAspectRatio(ratio);
        mGestureCropImageView.setImageToWrapCropBounds();
        if (listState.size() > 0) {
            viewState.setRotateIndex(listState.get(listState.size() - 1).getRotateIndex());
        }
        viewState.setRatioIndex(ratio);
        viewState.setIdRatio(id);
        listState.add(viewState);

    }

    private void setupRotateWidget() {
        mTextViewRotateAngle = findViewById(R.id.text_view_rotate);
        ((HorizontalProgressWheelView) findViewById(R.id.rotate_scroll_wheel))
                .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {

                    @Override
                    public void onScroll(float delta, float totalDistance) {
                        mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT);
                    }

                    @Override
                    public void onScrollEnd() {
                        viewState = new ViewState();
                        if (listState.size() > 0) {
                            viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
                        }
                        viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
                        listState.add(viewState);
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }

                    @Override
                    public void onScrollStart() {
                        mGestureCropImageView.cancelAllAnimations();
                    }
                });

        findViewById(R.id.wrapper_flip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewState = new ViewState();
                if (listState.size() > 0) {
                    viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
                    viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
                    viewState.setRotateIndex(listState.get(listState.size() - 1).getRotateIndex());
                }
                viewState.setIsFlip(true);
                listState.add(viewState);
                flipView(true);
            }
        });
        findViewById(R.id.wrapper_rotate_by_angle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewState = new ViewState();
                if (listState.size() > 0) {
                    viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
                    viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
                    viewState.setRotateIndex(listState.get(listState.size() - 1).getRotateIndex());
                }
                viewState.setIndexRotate90(true);
                listState.add(viewState);
                rotateByAngle(90);

            }
        });
    }


    private void setAngleText(float angle) {
        if (mTextViewRotateAngle != null) {
            if (angle == -0 || angle == -180) angle = 0;
            mTextViewRotateAngle.setText(String.format(Locale.getDefault(), "%.1f", angle));
        }
    }

    private void resetRotation() {
        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
//        mGestureCropImageView.resetImageToWrapCropBounds();
    }

    private void rotateByAngle(int angle) {
        mGestureCropImageView.postRotate(angle);
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void flipView(boolean flip) {
//        mGestureCropImageView.postFlip(flip);
        mGestureCropImageView.setImageToWrapCropBounds();
    }

    private void addBlockingView() {
        if (mBlockingView == null) {
            mBlockingView = new View(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mBlockingView.setLayoutParams(lp);
            mBlockingView.setClickable(true);
        }

        ((RelativeLayout) findViewById(R.id.ucrop_photobox)).addView(mBlockingView);
    }

    private void setupSaveImage() {
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropAndSaveImage();
            }
        });
    }

    protected void cropAndSaveImage() {
        mBlockingView.setClickable(true);
        supportInvalidateOptionsMenu();
        mGestureCropImageView.cropAndSaveImage(mCompressFormat, mCompressQuality, new BitmapCropCallback() {
            @Override
            public void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight) {
                setResultUri(resultUri, mGestureCropImageView.getTargetAspectRatio(), offsetX, offsetY, imageWidth, imageHeight);
                finish();
            }

            @Override
            public void onCropFailure(@NonNull Throwable t) {
                setResultError(t);
                finish();
            }
        });
    }

    private void undoCrop() {
        findViewById(R.id.iv_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listState.size() > 1) {
                    // last state is flip
                    if (listState.get(listState.size() - 1).getIsFlip()) {
//                        mGestureCropImageView.postFlip(true);
                    }
                    // last state is rotate90
                    if (listState.get(listState.size() - 1).getIndexRotate90()) {
                        mGestureCropImageView.postRotate(-90);
                    }
                    redoList.add(listState.get(listState.size() - 1));
                    listState.remove(listState.get(listState.size() - 1));

                    // last state is scroll rotate
                    float temp = mGestureCropImageView.getCurrentAngle();

                    if (listState.get(listState.size() - 1).getRotateIndex() != mGestureCropImageView.getCurrentAngle()) {
                        float tempRotate = listState.get(listState.size() - 1).getRotateIndex() - mGestureCropImageView.getCurrentAngle();
                        mGestureCropImageView.postRotate(tempRotate);
                    }
                    mGestureCropImageView.setTargetAspectRatio(listState.get(listState.size() - 1).getRatioIndex());
                    ratioAdapter.setId(listState.get(listState.size() - 1).getIdRatio());
                    float temp2 = mGestureCropImageView.getCurrentAngle();
                    mGestureCropImageView.setImageToWrapCropBounds();

                } else {
                    ratioAdapter.setId(0);
                    mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
                    mGestureCropImageView.setTargetAspectRatio(0.8f);
                    float temp = mGestureCropImageView.getCurrentAngle();
                    mGestureCropImageView.setImageToWrapCropBounds();
                }
            }
        });
    }

    private void redoCrop() {
        findViewById(R.id.iv_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (redoList.size() > 0) {
                    if (redoList.get(redoList.size() - 1).getIsFlip()) {
//                        mGestureCropImageView.postFlip(true);
                    }
                    if (redoList.get(redoList.size() - 1).getIndexRotate90()) {
                        mGestureCropImageView.postRotate(90);
                    }
                    float temp = mGestureCropImageView.getCurrentAngle();
                    if (redoList.get(redoList.size() - 1).getRotateIndex() != mGestureCropImageView.getCurrentAngle()) {
                        float tempRotate = redoList.get(redoList.size() - 1).getRotateIndex() - mGestureCropImageView.getCurrentAngle();
                        mGestureCropImageView.postRotate(tempRotate);
                    }
                    mGestureCropImageView.setTargetAspectRatio(redoList.get(redoList.size() - 1).getRatioIndex());
                    ratioAdapter.setId(redoList.get(redoList.size() - 1).getIdRatio());
                    mGestureCropImageView.setImageToWrapCropBounds();
                    listState.add(redoList.get(redoList.size() - 1));
                    redoList.remove(redoList.get(redoList.size() - 1));
                }
            }
        });
    }

    private void cancelCrop() {
        findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(getPackageName(),"com.example.photoediter.ui.main.MainActivity");
                startActivity(intent);

            }
        });
    }

    protected void setResultUri(Uri uri, float resultAspectRatio, int offsetX, int offsetY, int imageWidth, int imageHeight) {
        setResult(RESULT_OK, new Intent()
                .putExtra(UCrop.EXTRA_OUTPUT_URI, uri)
                .putExtra(UCrop.EXTRA_OUTPUT_CROP_ASPECT_RATIO, resultAspectRatio)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_X, offsetX)
                .putExtra(UCrop.EXTRA_OUTPUT_OFFSET_Y, offsetY)
        );
    }

    protected void setResultError(Throwable throwable) {
        setResult(UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, throwable));
    }

}

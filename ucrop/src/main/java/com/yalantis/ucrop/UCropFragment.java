package com.yalantis.ucrop;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;

import com.yalantis.ucrop.adapter.RatioAdapter;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.model.ViewState;
import com.yalantis.ucrop.util.RatioUtils;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.GestureCropImageView;
import com.yalantis.ucrop.view.OverlayView;
import com.yalantis.ucrop.view.TransformImageView;
import com.yalantis.ucrop.view.UCropView;
import com.yalantis.ucrop.view.widget.HorizontalProgressWheelView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ConstantConditions")
public class UCropFragment extends Fragment implements RatioAdapter.OnClickRatio {
    public static final int DEFAULT_COMPRESS_QUALITY = 100;
    public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;

    private static final long CONTROLS_ANIMATION_DURATION = 50;
    private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;

    private UCropFragmentCallback callback;
    private String desFileName = Constant.SAMPLE_CROPPED_IMAGE_NAME;
    private ArrayList<AspectRatio> aspectRatios;
    private Uri inputUri, outputUri;
    @ColorInt
    private int mRootViewBackgroundColor;
    private RatioAdapter ratioAdapter;
    private boolean mShowBottomControls;
    private UCropView mUCropView;
    private GestureCropImageView mGestureCropImageView;
    private OverlayView mOverlayView;
    private TextView mTextViewRotateAngle;

    private ViewState viewState;
    private final List<ViewState> listState = new ArrayList<>();
    private final List<ViewState> redoList = new ArrayList<>();
    private Bitmap.CompressFormat mCompressFormat = DEFAULT_COMPRESS_FORMAT;
    private int mCompressQuality = DEFAULT_COMPRESS_QUALITY;
    private Boolean checkUndo = false;
    private long mLastClickTime = 0;
    private int stateIdRatio = 0;
    private int countFlip;
    private int countRotate90;
    private float currentAngle = 0;
    private float currentRatio = 1;

    private ArrayList<AspectRatio> aspectRatioArrayList;
    private RecyclerView rvRatio;

    // count undo, redo
    private int countUndo;
    private int countRedo;
    private AppCompatImageView btnUndo;
    private AppCompatImageView btnRedo;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    public static UCropFragment newInstance(Bundle uCrop) {
        UCropFragment fragment = new UCropFragment();
        fragment.setArguments(uCrop);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof UCropFragmentCallback) {
            callback = (UCropFragmentCallback) getParentFragment();
        } else if (context instanceof UCropFragmentCallback) {
            callback = (UCropFragmentCallback) context;
        } else {
            throw new IllegalArgumentException(context.toString()
                    + " must implement UCropFragmentCallback");
        }
    }

    @Override
    public void onStart() {
        Log.d("TAG", "onStart uCrop: ");
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listState.size() > 0 || redoList.size() > 0) {
            listState.clear();
            redoList.clear();
        }
        desFileName = "";
        inputUri = null;
        outputUri = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(Constant.STATE_ID_RATIO, stateIdRatio);
        outState.putFloat(Constant.STATE_RATIO, currentRatio);

        Log.d("longloo", "currentRatio: " + currentRatio);
        Log.d("longloo", "stateIdRatio: " + stateIdRatio);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showLoadingDialog();

        setupViews(view);
        setImageData();
        setupSaveImage(view);
        undoCrop(view);
        redoCrop(view);
        cancelCrop(view);
        loadAllCache();
        if (savedInstanceState != null) {
            stateIdRatio = savedInstanceState.getInt(Constant.STATE_ID_RATIO);
            currentRatio = savedInstanceState.getFloat(Constant.STATE_RATIO);
            Log.d("longloo", "currentRatio1: " + currentRatio);
            Log.d("longloo", "stateIdRatio1: " + stateIdRatio);

            if (stateIdRatio != 0) mOverlayView.setFreestyleCropEnabled(false);
            else mOverlayView.setFreestyleCropEnabled(true);

            if (ratioAdapter != null) {
                ratioAdapter.setId(stateIdRatio);
                mGestureCropImageView.setTargetAspectRatio(currentRatio);
                if (savedInstanceState.getFloat(Constant.STATE_ROTATE) != 0)
                    rotateByAngle(currentAngle);
            }
//            else {
//                mGestureCropImageView.setTargetAspectRatio(currentRatio);
//                if (savedInstanceState.getFloat(Constant.STATE_ROTATE) != 0)
//                    rotateByAngle(currentAngle);
//            }
        }
    }

    private final TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
        @Override
        public void onLoadComplete() {
            mUCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
            callback.loadingProgress(false);
        }

        @Override
        public void onLoadFailure(@NonNull Exception e) {
        }

        @Override
        public void onRotate(float currentAngle) {
            setAngleText(currentAngle);
        }

        @Override
        public void onScale(float currentScale) {
        }
    };

    private void showLoadingDialog() {
        @SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.custom_loading_dialog, null);

        final Dialog loadingDialog = new Dialog(requireContext(), R.style.MaterialDialogSheet);
        loadingDialog.setContentView(v);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().setGravity(Gravity.CENTER);

        loadingDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
            }
        }, 2000);
    }

    public void setCallback(UCropFragmentCallback callback) {
        this.callback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.ucrop_fragment_photobox, container, false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        inputUri = null;
        outputUri = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("longlooo", "onStop: ");
    }

    /**
     * This method extracts all data from the incoming intent and setups views properly.
     */
    private void setImageData() {
        desFileName = Constant.SAMPLE_CROPPED_IMAGE_NAME + Calendar.getInstance().getTimeInMillis() + ".jpeg";
        inputUri = Uri.parse(getArguments().getString(Constant.REQUEST_URI_FROM_SPLASH));

        outputUri = Uri.fromFile(new File(requireActivity().getCacheDir(), desFileName));

        if (inputUri != null && outputUri != null) {
            try {
                mGestureCropImageView.setImageUri(inputUri, outputUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadAllCache() {
        File pathCacheDir = requireActivity().getCacheDir();
        File[] listCache = pathCacheDir.listFiles();
        for (File f : listCache) {
            f.delete();
        }
    }

    private void setupViews(View view) {
        viewState = new ViewState();
        viewState.setRatioIndex(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
        listState.add(viewState);

        countFlip = 0;
        countRotate90 = 0;

        countUndo = 0;
        countRedo = 0;
        btnUndo = view.findViewById(R.id.iv_undo);

        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Window window = requireActivity().getWindow();
        window.getDecorView().setSystemUiVisibility(View.VISIBLE);

        initiateRootViews(view);
        ViewGroup viewGroup = view.findViewById(R.id.ucrop_photobox);
        ViewGroup wrapper = viewGroup.findViewById(R.id.controls_wrapper);
        wrapper.setVisibility(View.VISIBLE);
        LayoutInflater.from(requireContext()).inflate(R.layout.ucrop_controls, wrapper, true);
        Transition mControlsTransition = new AutoTransition();
        mControlsTransition.setDuration(CONTROLS_ANIMATION_DURATION);
        mOverlayView.setFreestyleCropEnabled(true);
        setupAspectRatioWidget(view);
        setupRotateWidget(view);
    }

    private void initiateRootViews(View view) {
        mUCropView = view.findViewById(R.id.ucrop);
        mGestureCropImageView = mUCropView.getCropImageView();
        mOverlayView = mUCropView.getOverlayView();

        mGestureCropImageView.setTransformImageListener(mImageListener);
        callback.loadingProgress(true);
    }

    private void setupAspectRatioWidget(View view) {
        aspectRatioArrayList = RatioUtils.getRatioList(requireContext());
        rvRatio = view.findViewById(R.id.layout_aspect_ratio);

        if (rvRatio != null) {
            ratioAdapter = new RatioAdapter(aspectRatioArrayList, requireContext(), this);
            ratioAdapter.setId(0);
            rvRatio.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext());
            linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
            rvRatio.setLayoutManager(linearLayoutManager);
            rvRatio.setAdapter(ratioAdapter);
        }
    }

    @Override
    public void onChangeRatio(float ratio, int id) {
        checkUndo = true;
        viewState = new ViewState();
        if (mGestureCropImageView != null){
            if (id == 0) {
                mOverlayView.setFreestyleCropEnabled(true);
                mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
//                mGestureCropImageView.setTargetAspectRatio(ratio);
                viewState.setRatioIndex(mGestureCropImageView.getTargetAspectRatio());
            } else {
                mOverlayView.setFreestyleCropEnabled(false);
                mGestureCropImageView.setTargetAspectRatio(ratio);
                mGestureCropImageView.setImageToWrapCropBounds();
                viewState.setRatioIndex(ratio);
//                Log.d("Number4", "onChangeRatio: "+ratio);
            }
        }

//        if (listState.size() > 0) {
//            viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
//        }
        stateIdRatio = id;
        currentRatio = ratio;

        viewState.setIdRatio(id);
        listState.add(viewState);

        countUndo += 1;

//        setUndoImage();

        if (countUndo > 0) {
            btnUndo.setImageResource(R.drawable.ic_undo_selected);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setupRotateWidget(final View view) {
        mTextViewRotateAngle = view.findViewById(R.id.text_view_rotate);
        btnUndo = view.findViewById(R.id.iv_undo);
        ((HorizontalProgressWheelView) view.findViewById(R.id.rotate_scroll_wheel))
                .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
                    @Override
                    public void onScrollStart() {
                        mGestureCropImageView.cancelAllAnimations();
                    }

                    @Override
                    public void onScroll(float delta, float totalDistance) {
                        mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT);
                    }

                    @Override
                    public void onScrollEnd() {
                        countUndo += 1;

//                        setUndoImage();

                        if (countUndo > 0) {
                            btnUndo.setImageResource(R.drawable.ic_undo_selected);
                        }

                        checkUndo = true;
                        viewState = new ViewState();
                        if (listState.size() > 0) {
                            viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
                        }
                        viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
                        listState.add(viewState);
                        mGestureCropImageView.setImageToWrapCropBounds();
                        currentAngle = mGestureCropImageView.getCurrentAngle();
                    }
                });

        view.findViewById(R.id.wrapper_flip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countUndo += 1;

//                setUndoImage();

                if (countUndo > 0) {
                    btnUndo.setImageResource(R.drawable.ic_undo_selected);
                }

                countFlip += 1;
                if (countFlip > 1) countFlip = 0;
                flipView();
                checkUndo = true;
                viewState = new ViewState();
                if (listState.size() > 0) {
                    viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
                    viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
                    viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
//                    viewState.setRotateIndex(listState.get(listState.size() - 1).getRotateIndex());
                }
                viewState.setIsFlip(true);
                listState.add(viewState);
                setAngleText(mGestureCropImageView.getCurrentAngle());
            }
        });

        view.findViewById(R.id.wrapper_rotate_by_angle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countUndo += 1;

//                setUndoImage();

                if (countUndo > 0) {
                    btnUndo.setImageResource(R.drawable.ic_undo_selected);
                }

                countRotate90 += 1;
                if (countRotate90 > 3) countRotate90 = 0;
                rotateByAngle(90);
                checkUndo = true;
                viewState = new ViewState();
                if (listState.size() > 0) {
                    viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
                    viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
                    viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
//                    viewState.setRotateIndex(listState.get(listState.size() - 1).getRotateIndex());
                }
                viewState.setIndexRotate90(true);
                listState.add(viewState);
                currentAngle = mGestureCropImageView.getCurrentAngle();
            }
        });
    }

    private void flipView() {
        mGestureCropImageView.flipImageView();
    }

    private void setAngleText(float angle) {
        if (mTextViewRotateAngle != null) {
            if (angle == -0 || angle == -180) angle = 0;
            mTextViewRotateAngle.setText(String.format(Locale.getDefault(), "%.1f°", angle));
            if (mTextViewRotateAngle.getText().toString().equals("-0.0°")) {
                mTextViewRotateAngle.setText("0.0°");
            } else if (mTextViewRotateAngle.getText().toString().equals("-180.0°")) {
                mTextViewRotateAngle.setText("180.0°");
            }
        }
    }

    private void rotateByAngle(float angle) {
        mGestureCropImageView.postRotate(angle);
        mGestureCropImageView.setImageToWrapCropBounds();
    }
//
//    private boolean onTouchEvent(MotionEvent event) {
//        long pressTime = 0, eventTime = 0;
//        int eventAction = event.getAction();
//        switch (eventAction) {
//            case MotionEvent.ACTION_DOWN: {
//                pressTime = event.getDownTime();
//                break;
//            }
//            case MotionEvent.ACTION_UP: {
//                eventTime = event.getEventTime();
//                break;
//            }
//        }
//        Log.d("longloo", "onTouchEvent: " + pressTime + "   " + eventTime + "   ");
//        return false;
//    }

    private void setupSaveImage(final View view) {
//        view.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                long pressTime = 0, eventTime = 0;
////                int eventAction = event.getAction();
////                switch (eventAction) {
////                    case MotionEvent.ACTION_DOWN: {
////                        pressTime = event.getDownTime();
////                        Log.d("longloo", "onTouchEvent1: " + pressTime);
////
//////                        break;
////                    }
////                    case MotionEvent.ACTION_UP: {
////                        eventTime = event.getEventTime();
////                        Log.d("longloo", "onTouchEvent2: " + eventTime);
////
//////                        break;
////                    }
////                }
//                Log.d("longloo", "onTouchEvent: " + event.getDownTime() + "   " + event.getEventTime() + "   " + SystemClock.elapsedRealtime());
//                return true;
//            }
//        });

//        Log.d("longloo", "mLastClickTime: " + mLastClickTime + "     " + SystemClock.elapsedRealtime());
//        mLastClickTime = SystemClock.elapsedRealtime();
//        Log.d("longloo", "mLastClickTime1: " + mLastClickTime + "     " + SystemClock.elapsedRealtime());
//
//        if ((SystemClock.elapsedRealtime() - mLastClickTime) >= 4000) {
//            Log.d("longloo", "mLastClickTime2: " + mLastClickTime + "     " + SystemClock.elapsedRealtime());
//            Log.d("longloo", "mLastClickTime3: " + (SystemClock.elapsedRealtime() - mLastClickTime));
//            view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    cropAndSaveImage();
//                }
//            });
//        } else {
//            Toast.makeText(requireContext(), "You clicked too quick", Toast.LENGTH_SHORT).show();
//        }
//        if (SystemClock.elapsedRealtime() - mLastClickTime < )

        view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Button Done", Toast.LENGTH_SHORT).show();
//                mLastClickTime = SystemClock.elapsedRealtime();
                Log.d("longloo", "mLastClickTime: " + mLastClickTime + "     " + SystemClock.elapsedRealtime());
                if ((SystemClock.elapsedRealtime() - mLastClickTime) < 4000) {
                    Log.d("longloo", "onClick: " + (SystemClock.elapsedRealtime() - mLastClickTime));
                    v.findViewById(R.id.btn_done).setClickable(false);
                } else {
                    Log.d("longloo", "onClick1: " + (SystemClock.elapsedRealtime() - mLastClickTime));
                    mLastClickTime = SystemClock.elapsedRealtime();
                    Log.d("longloo", "onClick2: " + (SystemClock.elapsedRealtime() - mLastClickTime));
                    v.findViewById(R.id.btn_done).setClickable(true);
                    cropAndSaveImage();
                }
            }
        });
    }

    protected void cropAndSaveImage() {
        callback.loadingProgress(true);
        RectF rectF = mOverlayView.getCropViewRect();
        Bitmap bitmap = Bitmap.createBitmap(mGestureCropImageView.getWidth(), mGestureCropImageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mGestureCropImageView.draw(canvas);
        Bitmap bm = Bitmap.createBitmap(bitmap, (int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
        String path = saveBitmapToLocal(bm, requireContext());
        if (path != null) {
            callback.onCrop(path);
        }
    }

    public String saveBitmapToLocal(Bitmap bm, Context context) {
        String path = null;
        try {
            File file = new File(context.getCacheDir(), System.currentTimeMillis() + ".jpg");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            path = file.getAbsolutePath();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private void undoCrop(final View view) {
        btnUndo = view.findViewById(R.id.iv_undo);
        btnRedo = view.findViewById(R.id.iv_redo);

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkUndo) {
                    if (listState.size() > 1) {
                        // last state is flip
                        if (listState.get(listState.size() - 1).getIsFlip()) {
//                            mGestureCropImageView.postFlip(true);
                            mGestureCropImageView.flipImageView();
                        }

                        // last state is rotate90
                        if (listState.get(listState.size() - 1).getIndexRotate90()) {
                            mGestureCropImageView.postRotate(-90);
                        }

                        redoList.add(listState.get(listState.size() - 1));
                        listState.remove(listState.get(listState.size() - 1));

                        btnRedo.setImageResource(R.drawable.ic_redo_selected);
                        countRedo += 1;
//                        Toast.makeText(requireContext(), "countRedo--" + countRedo, Toast.LENGTH_SHORT).show();

                        // last state is scroll rotate
                        float temp = mGestureCropImageView.getCurrentAngle();

                        if (listState.get(listState.size() - 1).getRotateIndex() != mGestureCropImageView.getCurrentAngle()) {
                            float tempRotate = listState.get(listState.size() - 1).getRotateIndex() - mGestureCropImageView.getCurrentAngle();
                            mGestureCropImageView.postRotate(tempRotate);
                        }
                        mGestureCropImageView.setTargetAspectRatio(listState.get(listState.size() - 1).getRatioIndex());
                        ratioAdapter.setId(listState.get(listState.size() - 1).getIdRatio());

                        mGestureCropImageView.setImageToWrapCropBounds();
                        setAngleText(mGestureCropImageView.getCurrentAngle());

                        countUndo -= 1;
                        if (countUndo == 0) {
                            btnUndo.setImageResource(R.mipmap.ic_undo);
                        }
                    } else {
//                        ratioAdapter.setId(0);
                        mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
//                        mGestureCropImageView.setTargetAspectRatio(1.0f);
                        mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
                        mGestureCropImageView.setImageToWrapCropBounds();
                    }
                }
            }
        });
    }

    private void redoCrop(View view) {
        btnRedo = view.findViewById(R.id.iv_redo);
        btnUndo = view.findViewById(R.id.iv_undo);

        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (redoList.size() > 0) {
                    if (redoList.get(redoList.size() - 1).getIsFlip()) {
//                        mGestureCropImageView.postFlip(true);
                        mGestureCropImageView.flipImageView();
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
                    setAngleText(mGestureCropImageView.getCurrentAngle());

                    countRedo -= 1;
                    countUndo += 1;
                    if (countUndo > 0) {
                        btnUndo.setImageResource(R.drawable.ic_undo_selected);
                    }
//                    Toast.makeText(requireContext(), "countRedo--" + countRedo, Toast.LENGTH_SHORT).show();
                    if (countRedo == 0) {
                        btnRedo.setImageResource(R.mipmap.ic_redo);
                    }
                }
            }
        });
    }

    private void cancelCrop(View view) {
        view.findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void setUndoImage() {
        if (countUndo > 0) {
//            btnUndo.setImageDrawable(R.drawable.ic_undo_selected);
            Toast.makeText(requireContext(), "count " + countUndo, Toast.LENGTH_SHORT).show();
        }
    }
}

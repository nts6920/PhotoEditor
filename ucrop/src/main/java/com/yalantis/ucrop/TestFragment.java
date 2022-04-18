package com.yalantis.ucrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;

import com.yalantis.ucrop.adapter.RatioAdapter;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.model.ViewState;
import com.yalantis.ucrop.util.RatioUtils;
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

public class TestFragment {
//    @SuppressWarnings("ConstantConditions")
//    public class UCropFragment extends Fragment implements RatioAdapter.OnClickRatio {
//
//        public static final int DEFAULT_COMPRESS_QUALITY = 100;
//        public static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
//        public static final String TAG = "UCropFragment";
//        private String desFileName;
//        private static final long CONTROLS_ANIMATION_DURATION = 50;
//        private static final int ROTATE_WIDGET_SENSITIVITY_COEFFICIENT = 42;
//        private Boolean checkUndo = false;
//        private Uri inputUri, outputUri;
//        private UCropFragmentCallback callback;
//        private RatioAdapter ratioAdapter;
//        private UCropView mUCropView;
//        private GestureCropImageView mGestureCropImageView;
//        private OverlayView mOverlayView;
//        private TextView mTextViewRotateAngle;
//        private ViewState viewState;
//        private ArrayList<AspectRatio> aspectRatioArrayList;
//        private final List<ViewState> listState = new ArrayList<>();
//        private final List<ViewState> redoList = new ArrayList<>();
//        private long mLastClickTime = 0;
//        private int stateIdRatio = -1;
//        private RecyclerView rvRatio;
//        private int countFlip;
//        private int countRotate90;
//        private float currentAngle = 0;
//        private float currentRatio = 1;
//
//        static {
//            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//        }
//
//        public static com.yalantis.ucrop.UCropFragment newInstance(Bundle uCrop) {
//            com.yalantis.ucrop.UCropFragment fragment = new com.yalantis.ucrop.UCropFragment();
//            fragment.setArguments(uCrop);
//            return fragment;
//        }
//
//        @Override
//        public void onAttach(@NonNull Context context) {
//            super.onAttach(context);
//            if (getParentFragment() instanceof UCropFragmentCallback)
//                callback = (UCropFragmentCallback) getParentFragment();
//            else if (context instanceof UCropFragmentCallback)
//                callback = (UCropFragmentCallback) context;
//            else
//                throw new IllegalArgumentException(context.toString()
//                        + " must implement UCropFragmentCallback");
//        }
//
//        @Override
//        public void onStart() {
//            Log.d("TAG", "onStart uCrop: ");
//            super.onStart();
//        }
//
//        @Override
//        public void onStop() {
//            super.onStop();
//            Log.d("TAG", "onStop UCrop: ");
//
//        }
//
//        @Override
//        public void onDestroyView() {
//            super.onDestroyView();
//            if (listState.size() > 0 || redoList.size() > 0) {
//                listState.clear();
//                redoList.clear();
//            }
//            desFileName = "";
//            inputUri = null;
//            outputUri = null;
//            Log.d("TAG", "onDestroyView UCrop: ");
//
//        }
//
//        @Override
//        public void onSaveInstanceState(@NonNull Bundle outState) {
//            outState.putInt(Constant.STATE_ID_RATIO, stateIdRatio);
//            outState.putFloat(Constant.STATE_RATIO, currentRatio);
//            Log.d("TAG", "onSaveInstanceState angle: " + mGestureCropImageView.getCurrentAngle());
//            super.onSaveInstanceState(outState);
//        }
//
//        @Override
//        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//            super.onViewCreated(view, savedInstanceState);
//            setupViews(view);
//            setImageData();
//            setupSaveImage(view);
//            undoCrop(view);
//            redoCrop(view);
//            cancelCrop(view);
//            loadAllCache();
//            if (savedInstanceState != null) {
//                stateIdRatio = savedInstanceState.getInt(Constant.STATE_ID_RATIO);
//                currentRatio = savedInstanceState.getFloat(Constant.STATE_RATIO);
//                Log.d(TAG, "count uCop: Flip: "+countFlip +" Rotate90: "+countRotate90);
//                Log.d("TAG", "savedInstanceState ratio adapter: "+ratioAdapter);
//
//                if (stateIdRatio != 0)
//                    mOverlayView.setFreestyleCropEnabled(false);
//                else mOverlayView.setFreestyleCropEnabled(true);
//
//                if (ratioAdapter != null) {
//                    ratioAdapter.setId(stateIdRatio);
//                }
//                if (mGestureCropImageView != null){
//                    mGestureCropImageView.setTargetAspectRatio(currentRatio);
//                    if (savedInstanceState.getFloat(Constant.STATE_ROTATE) != 0)
//                        rotateByAngle(currentAngle);
//                    Log.d(TAG, "onViewCreated: " + savedInstanceState.getFloat(Constant.STATE_ROTATE));
//                }
//            }
//        }
//
//
//        public void setCallback(UCropFragmentCallback callback) {
//            this.callback = callback;
//        }
//
//        @Nullable
//        @Override
//        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.ucrop_fragment_photobox, container, false);
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//            return rootView;
//        }
//
//        public void setupViews(View view) {
//            viewState = new ViewState();
//            listState.add(viewState);
//            countFlip = 0;
//            countRotate90 = 0;
//            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            Window window = requireActivity().getWindow();
//            window.getDecorView().setSystemUiVisibility(View.VISIBLE);
//            initiateRootViews(view);
////        callback.loadingProgress(true);
//            ViewGroup viewGroup = view.findViewById(R.id.ucrop_photobox);
//            ViewGroup wrapper = viewGroup.findViewById(R.id.controls_wrapper);
//            wrapper.setVisibility(View.VISIBLE);
//            LayoutInflater.from(requireContext()).inflate(R.layout.ucrop_controls, wrapper, true);
//            Transition mControlsTransition = new AutoTransition();
//            mControlsTransition.setDuration(CONTROLS_ANIMATION_DURATION);
//            mOverlayView.setFreestyleCropEnabled(true);
//            setupAspectRatioWidget(view);
//            setupRotateWidget(view);
//        }
//
//        private void setImageData() {
//            desFileName = Constant.SAMPLE_CROPPED_IMAGE_NAME + Calendar.getInstance().getTimeInMillis() + Constant.TYPE_JPG;
//            inputUri = Uri.parse(getArguments().getString(Constant.REQUEST_URI_FROM_SPLASH));
//            outputUri = Uri.fromFile(new File(requireActivity().getCacheDir(), desFileName));
//            if (inputUri != null && outputUri != null) {
//                try {
//                    mGestureCropImageView.setImageUri(inputUri, outputUri);
//                } catch (Exception ignored) {
//                }
//            }
//        }
//
//        public void loadAllCache() {
//            File pathCacheDir = requireActivity().getCacheDir();
//            File[] listCache = pathCacheDir.listFiles();
//            for (File f : listCache) {
//                f.delete();
//            }
//        }
//
//        private void cancelCrop(View view) {
//            view.findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    requireActivity().onBackPressed();
//                }
//            });
//        }
//
//        private void initiateRootViews(View view) {
//            mUCropView = view.findViewById(R.id.ucrop);
//            mGestureCropImageView = mUCropView.getCropImageView();
//            mOverlayView = mUCropView.getOverlayView();
//            mGestureCropImageView.setTransformImageListener(mImageListener);
//            callback.loadingProgress(true);
//        }
//
//        TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
//            @Override
//            public void onRotate(float currentAngle) {
//                setAngleText(currentAngle);
//            }
//
//            @Override
//            public void onScale(float currentScale) {
//            }
//
//            @Override
//            public void onLoadComplete() {
//                mUCropView.animate().alpha(1).setDuration(300).setInterpolator(new AccelerateInterpolator());
//                callback.loadingProgress(false);
//            }
//
//            @Override
//            public void onLoadFailure(@NonNull Exception e) {
//            }
//
//        };
//
//        private void setupAspectRatioWidget(View view) {
//            aspectRatioArrayList = RatioUtils.getRatioList(requireContext());
//            rvRatio = view.findViewById(R.id.layout_aspect_ratio);
//            if (rvRatio != null) {
//                ratioAdapter = new RatioAdapter(aspectRatioArrayList, requireContext(), this);
//                LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
//                layoutManager.setOrientation(RecyclerView.HORIZONTAL);
//                rvRatio.setLayoutManager(layoutManager);
//                rvRatio.setAdapter(ratioAdapter);
//            }
//        }
//
//        @Override
//        public void onChangeRatio(float ratio, int id) {
//            checkUndo = true;
//            viewState = new ViewState();
//            if (id == 0) {
//                mOverlayView.setFreestyleCropEnabled(true);
//                mGestureCropImageView.setTargetAspectRatio(ratio);
//                viewState.setRatioIndex(mGestureCropImageView.getTargetAspectRatio());
//            } else {
//                mOverlayView.setFreestyleCropEnabled(false);
//                mGestureCropImageView.setTargetAspectRatio(ratio);
//                mGestureCropImageView.setImageToWrapCropBounds();
//                viewState.setRatioIndex(ratio);
//            }
//            if (listState.size() > 0) {
//                viewState.setRotateIndex(listState.get(listState.size() - 1).getRotateIndex());
//            }
//            stateIdRatio = id;
//            currentRatio = ratio;
//            viewState.setIdRatio(id);
//            listState.add(viewState);
//
//        }
//
//        private void setupRotateWidget(View view) {
//            mTextViewRotateAngle = view.findViewById(R.id.text_view_rotate);
//            ((HorizontalProgressWheelView) view.findViewById(R.id.rotate_scroll_wheel))
//                    .setScrollingListener(new HorizontalProgressWheelView.ScrollingListener() {
//                        @Override
//                        public void onScroll(float delta, float totalDistance) {
//                            mGestureCropImageView.postRotate(delta / ROTATE_WIDGET_SENSITIVITY_COEFFICIENT);
//                        }
//
//                        @Override
//                        public void onScrollEnd() {
//                            checkUndo = true;
//                            viewState = new ViewState();
//                            if (listState.size() > 0) {
//                                viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
//                            }
//                            viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
//                            listState.add(viewState);
//                            mGestureCropImageView.setImageToWrapCropBounds();
//                            currentAngle = mGestureCropImageView.getCurrentAngle();
//                        }
//
//                        @Override
//                        public void onScrollStart() {
//                            mGestureCropImageView.cancelAllAnimations();
//                        }
//                    });
//            view.findViewById(R.id.wrapper_flip).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    countFlip +=1;
//                    if (countFlip>1) countFlip = 0;
//                    flipView();
//                    checkUndo = true;
//                    viewState = new ViewState();
//                    if (listState.size() > 0) {
//                        viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
//                        viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
//                        viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
//                    }
//                    viewState.setIsFlip(true);
//                    listState.add(viewState);
//                    setAngleText(mGestureCropImageView.getCurrentAngle());
//                }
//            });
//            view.findViewById(R.id.wrapper_rotate_by_angle).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    countRotate90+=1;
//                    if (countRotate90>3) countRotate90 = 0;
//                    rotateByAngle(90);
//                    checkUndo = true;
//                    viewState = new ViewState();
//                    if (listState.size() > 0) {
//                        viewState.setIdRatio(listState.get(listState.size() - 1).getIdRatio());
//                        viewState.setRatioIndex(listState.get(listState.size() - 1).getRatioIndex());
////                    viewState.setRotateIndex(listState.get(listState.size()-1).getRotateIndex()+90f);
//                        viewState.setRotateIndex(mGestureCropImageView.getCurrentAngle());
//                    }
//                    viewState.setIndexRotate90(true);
//                    listState.add(viewState);
//                    currentAngle = mGestureCropImageView.getCurrentAngle();
//
//                }
//            });
//        }
//
//        private void flipView() {
//            mGestureCropImageView.flipImageView();
//        }
//
//        private void setAngleText(float angle) {
//            if (mTextViewRotateAngle != null) {
//                mTextViewRotateAngle.setText(String.format(Locale.getDefault(), "%.1f", angle));
//                if (mTextViewRotateAngle.getText().toString().equals("-0,0"))
//                    mTextViewRotateAngle.setText("0,0");
//            }
//        }
//
//        private void resetRotation() {
//            mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
//            mGestureCropImageView.setImageToWrapCropBounds();
//        }
//
//        private void rotateByAngle(float angle) {
//            mGestureCropImageView.postRotate(angle);
//            mGestureCropImageView.setImageToWrapCropBounds();
//        }
//
//        private void setupSaveImage(View view) {
//            view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (SystemClock.elapsedRealtime() - mLastClickTime < 4000) {
//                        view.findViewById(R.id.btn_done).setClickable(false);
//                        return;
//                    }
//                    mLastClickTime = SystemClock.elapsedRealtime();
//                    view.findViewById(R.id.btn_done).setClickable(true);
//                    cropAndSaveImage();
//                }
//            });
//        }
//
//        public void cropAndSaveImage() {
//            callback.loadingProgress(true);
//            RectF rectF = mOverlayView.getCropViewRect();
//            Bitmap bitmap = Bitmap.createBitmap(mGestureCropImageView.getWidth(), mGestureCropImageView.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            mGestureCropImageView.draw(canvas);
//            Bitmap bm = Bitmap.createBitmap(bitmap, (int) rectF.left, (int) rectF.top, (int) (rectF.right - rectF.left), (int) (rectF.bottom - rectF.top));
//            String path = saveBitmapToLocal(bm, requireContext());
//            if (path != null) {
//                callback.onCrop(path);
//            }
//
////        mGestureCropImageView.cropAndSaveImage(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, new BitmapCropCallback() {
////
////            @Override
////            public void onBitmapCropped(@NonNull Uri resultUri, int offsetX, int offsetY, int imageWidth, int imageHeight) {
////                callback.onCrop(resultUri);
////                callback.loadingProgress(false);
////            }
////
////            @Override
////            public void onCropFailure(@NonNull Throwable t) {
////                Toast.makeText(requireContext(), requireContext().getString(R.string.ucrop_fail), Toast.LENGTH_SHORT).show();
////            }
////        });
//        }
//
//        private void undoCrop(View view) {
//            view.findViewById(R.id.iv_undo).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (checkUndo) {
//                        if (listState.size() > 1) {
//                            // last state is flip
//                            if (listState.get(listState.size() - 1).getIsFlip()) {
//                                mGestureCropImageView.flipImageView();
//                            }
//                            // last state is rotate90
//                            if (listState.get(listState.size() - 1).getIndexRotate90()) {
//                                mGestureCropImageView.postRotate(-90);
//                            }
//                            redoList.add(listState.get(listState.size() - 1));
//                            listState.remove(listState.get(listState.size() - 1));
//
//                            // last state is scroll rotate
//                            float temp = mGestureCropImageView.getCurrentAngle();
//                            if (listState.get(listState.size() - 1).getRotateIndex() != mGestureCropImageView.getCurrentAngle()) {
//                                float tempRotate = listState.get(listState.size() - 1).getRotateIndex() - mGestureCropImageView.getCurrentAngle();
//                                mGestureCropImageView.postRotate(tempRotate);
//                            }
//                            mGestureCropImageView.setTargetAspectRatio(listState.get(listState.size() - 1).getRatioIndex());
//                            ratioAdapter.setId(listState.get(listState.size() - 1).getIdRatio());
//
//                            mGestureCropImageView.setImageToWrapCropBounds();
//                            setAngleText(mGestureCropImageView.getCurrentAngle());
//
//                        } else {
//                            ratioAdapter.setId(0);
//                            mGestureCropImageView.postRotate(-mGestureCropImageView.getCurrentAngle());
//                            mGestureCropImageView.setImageToWrapCropBounds();
////                        mGestureCropImageView.setTargetAspectRatio(1f/1f);
//                        }
//
//                    }
//                }
//            });
//        }
//
//        private void redoCrop(View view) {
//            view.findViewById(R.id.iv_redo).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (redoList.size() > 0) {
//                        if (redoList.get(redoList.size() - 1).getIsFlip()) {
//                            mGestureCropImageView.flipImageView();
//                        }
//                        if (redoList.get(redoList.size() - 1).getIndexRotate90()) {
//                            mGestureCropImageView.postRotate(90);
//                        }
//                        if (redoList.get(redoList.size() - 1).getRotateIndex() != mGestureCropImageView.getCurrentAngle()) {
//                            float tempRotate = redoList.get(redoList.size() - 1).getRotateIndex() - mGestureCropImageView.getCurrentAngle();
//                            mGestureCropImageView.postRotate(tempRotate);
//                        }
//                        mGestureCropImageView.setTargetAspectRatio(redoList.get(redoList.size() - 1).getRatioIndex());
//                        ratioAdapter.setId(redoList.get(redoList.size() - 1).getIdRatio());
//                        mGestureCropImageView.setImageToWrapCropBounds();
//                        listState.add(redoList.get(redoList.size() - 1));
//                        redoList.remove(redoList.get(redoList.size() - 1));
//                        setAngleText(mGestureCropImageView.getCurrentAngle());
//                    }
//                }
//            });
//        }
//        public String saveBitmapToLocal(Bitmap bm, Context context) {
//            String path = null;
//            try {
//                File file = new File(context.getCacheDir(), System.currentTimeMillis() + ".jpg");
//                if (!file.exists()) {
//                    file.createNewFile();
//                }
//                FileOutputStream fos = new FileOutputStream(file);
//                bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                fos.flush();
//                fos.close();
//                path = file.getAbsolutePath();
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return path;
//        }
//
//    }


}

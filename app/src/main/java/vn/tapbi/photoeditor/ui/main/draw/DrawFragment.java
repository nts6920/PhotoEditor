package vn.tapbi.photoeditor.ui.main.draw;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;
import vn.tapbi.photoeditor.R;
import vn.tapbi.photoeditor.common.Constant;
import vn.tapbi.photoeditor.databinding.DrawFragmentBinding;
import vn.tapbi.photoeditor.feature.drawable.DrawingView;
import vn.tapbi.photoeditor.ui.base.BaseBindingFragment;
import vn.tapbi.photoeditor.ui.main.edit.EditFragment;

public class DrawFragment extends BaseBindingFragment<DrawFragmentBinding, DrawViewModel> {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private Boolean checkShowSize, checkShowColor;

    public static DrawFragment newInstance() {
        return new DrawFragment();
    }

    @Override
    protected Class<DrawViewModel> getViewModel() {
        return DrawViewModel.class;
    }

    @Override
    public int getLayoutId() {
        return R.layout.draw_fragment;
    }

    @Override
    protected void onCreatedView(View view, Bundle savedInstanceState) {
        binding.tools.tvWidgetName.setText(requireContext().getString(R.string.draw));

        addFragment();
        setupView();
        observeView();
        drawToolsEvent();

        if (savedInstanceState != null) {
            // draw color
            checkShowColor = savedInstanceState.getBoolean(Constant.SHOW_DRAW_COLOR);
            mainViewModel.isClickColorDraw.setValue(checkShowColor);

            // draw size
            checkShowSize = savedInstanceState.getBoolean(Constant.SHOW_DRAW_SIZE);
            mainViewModel.isClickSizeDraw.setValue(checkShowSize);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(Constant.SHOW_DRAW_COLOR, checkShowColor);
        outState.putBoolean(Constant.SHOW_DRAW_SIZE, checkShowSize);

        super.onSaveInstanceState(outState);
    }

    private void setupView() {
        binding.btnDrawColor.setOnClickListener(v -> {
            mainViewModel.setIsClickColorDraw(true);
        });
        binding.btnDrawSize.setOnClickListener(v -> mainViewModel.setIsClickSizeDraw(true));
    }

    private void observeView() {
        mainViewModel.isClickColorDraw.observe(getViewLifecycleOwner(), isDrawColorClicked -> {
            if (isDrawColorClicked) {
                showFragment(0);
                checkShowColor = true;
            } else {
                hideFragment(0);
                checkShowColor = false;
            }
        });

        mainViewModel.isClickSizeDraw.observe(getViewLifecycleOwner(), isDrawSizeClicked -> {
            if (isDrawSizeClicked) {
                showFragment(1);
                checkShowSize = true;
            } else {
                hideFragment(1);
                checkShowSize = false;
            }
        });
    }

    private void drawToolsEvent() {
//        int undoCount = editFragment.binding.imgToDraw.getCountUndo();

        binding.tools.icUndo.setOnClickListener(v -> {
//            if (checkShowColor) {
//                mainViewModel.setIsUndoDraw(false);
//            } else {
//                mainViewModel.setIsUndoDraw(true);
//            }
            mainViewModel.setIsUndoDraw(true);
        });

        binding.tools.icRedo.setOnClickListener(v -> {
//            if (checkShowSize) {
//                mainViewModel.setIsRedoDraw(false);
//            } else {
//                mainViewModel.setIsRedoDraw(true);
//            }
            mainViewModel.setIsRedoDraw(true);
        });

        binding.tools.icRemoveAll.setOnClickListener(v -> {
            mainViewModel.setIsDeleteDraw(true);
            mainViewModel.setIsClickItemDraw(false);
        });

        binding.tools.icDone.setOnClickListener(v -> {
            mainViewModel.setIsClickItemDraw(false);
        });
    }

    private void addFragment() {
        DrawColorFragment drawColorFragment = DrawColorFragment.newInstance();
        DrawSizeFragment drawSizeFragment = DrawSizeFragment.newInstance();

        fragmentList.add(drawColorFragment);
        fragmentList.add(drawSizeFragment);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.rl_draw, drawColorFragment).hide(drawColorFragment);
        fragmentTransaction.add(R.id.rl_draw, drawSizeFragment).hide(drawSizeFragment);

        fragmentTransaction.commit();
    }

    private void showFragment(int fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        for (int i = 0; i < fragmentList.size(); i++) {
            if (i == fragment) fragmentTransaction.show(fragmentList.get(fragment));
            else fragmentTransaction.hide(fragmentList.get(i));
        }

        fragmentTransaction.commit();
    }

    private void hideFragment(int fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragmentList.get(fragment).isAdded()) {
            fragmentTransaction.hide(fragmentList.get(fragment));
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onPermissionGranted() {
    }
}
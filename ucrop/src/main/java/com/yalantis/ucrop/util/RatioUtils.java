package com.yalantis.ucrop.util;

import android.content.Context;

import com.yalantis.ucrop.R;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;

import java.util.ArrayList;

public class RatioUtils {
    public static ArrayList<AspectRatio> getRatioList(Context context){
        ArrayList<AspectRatio> aspectRatioList = new ArrayList<>();
        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_free),
                CropImageView.SOURCE_IMAGE_ASPECT_RATIO, CropImageView.SOURCE_IMAGE_ASPECT_RATIO, R.mipmap.ic_white_free_ratio, R.mipmap.ic_blue_free_ratio));
//        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_free),
//                1, 1, R.mipmap.ic_white_free_ratio, R.mipmap.ic_blue_free_ratio));
        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_square), 1, 1, R.mipmap.ic_white_square_ratio, R.mipmap.ic_blue_square_ratio));
        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_story), 3, 4, R.mipmap.ic_white_story_ratio, R.mipmap.ic_blue_story_ratio));
        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_ins), 3, 2, R.mipmap.ic_white_ins_ratio, R.mipmap.ic_blue_ins_ratio));
        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_post), 16, 9, R.mipmap.ic_white_fb_ratio, R.mipmap.ic_blue_fb_ratio));
        aspectRatioList.add(new AspectRatio(context.getString(R.string.ucrop_cover), 21, 9, R.mipmap.ic_white_cover_ratio, R.mipmap.ic_blue_cover_ratio));
        return aspectRatioList;
    }
}

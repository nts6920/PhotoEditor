package com.xiaopo.flying.sticker;

import android.graphics.Typeface;

public class TextStickerState {
//    private Sticker sticker;
//    private int alpha;
//    private String color;
//    private String path;
//
//    public TextStickerState(Sticker sticker, int alpha, String color, String path) {
//        this.sticker = sticker;
//        this.alpha = alpha;
//        this.color = color;
//        this.path = path;
//    }
//
//    public String getColor() {
//        return color;
//    }
//
//    public void setColor(String color) {
//        this.color = color;
//    }
//
//    public Sticker getSticker() {
//        return sticker;
//    }
//
//    public void setSticker(Sticker sticker) {
//        this.sticker = sticker;
//    }
//
//    public int getAlpha() {
//        return alpha;
//    }
//
//    public void setAlpha(int alpha) {
//        this.alpha = alpha;
//    }
//
//    public String getPath() {
//        return path;
//    }
//
//    public void setPath(String path) {
//        this.path = path;
//    }
private int color;
    private int colorBackground;
    private Typeface font;


    public TextStickerState() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public void setColorBackground(int colorBackground) {
        this.colorBackground = colorBackground;
    }

    public Typeface getFont() {
        return font;
    }

    public void setFont(Typeface font) {
        this.font = font;
    }

    public TextStickerState(int color, int colorBackground, Typeface font) {
        this.color = color;
        this.colorBackground = colorBackground;
        this.font = font;
    }
}

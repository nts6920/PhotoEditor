package com.xiaopo.flying.sticker;

public class StickerState {
    private Sticker sticker;

    public StickerState() {
    }

    public StickerState(Sticker sticker) {
        this.sticker = sticker;
    }

    public Sticker getSticker() {
        return sticker;
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
    }
}

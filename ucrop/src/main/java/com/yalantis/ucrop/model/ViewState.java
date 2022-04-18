package com.yalantis.ucrop.model;

public class ViewState {
    private float rotateIndex;
    private boolean indexRotate90;
    private boolean isFlip;
    private float ratioIndex;
    private int idRatio;

    public ViewState() {
        rotateIndex = 0;
        isFlip = false;
        indexRotate90 = false;
        ratioIndex = 0.8f;
        idRatio = 0;
    }

    public int getIdRatio() {
        return idRatio;
    }

    public void setIdRatio(int idRatio) {
        this.idRatio = idRatio;
    }

    public float getRotateIndex() {
        return rotateIndex;
    }

    public void setRotateIndex(float rotateIndex) {
        this.rotateIndex = rotateIndex;
    }

    public boolean getIndexRotate90() {
        return indexRotate90;
    }

    public void setIndexRotate90(boolean indexRotate90) {
        this.indexRotate90 = indexRotate90;
    }

    public boolean getIsFlip() {
        return isFlip;
    }

    public void setIsFlip(boolean flip) {
        this.isFlip = flip;
    }

    public float getRatioIndex() {
        return ratioIndex;
    }

    public void setRatioIndex(float ratioIndex) {
        this.ratioIndex = ratioIndex;
    }
}

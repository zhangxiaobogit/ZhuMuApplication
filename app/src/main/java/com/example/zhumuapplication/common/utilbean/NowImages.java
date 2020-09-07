package com.example.zhumuapplication.common.utilbean;

public class NowImages {
    byte[] nowImage, nowImageHead;

    public NowImages(byte[] nowImage, byte[] nowImageHead) {
        this.nowImage = nowImage;
        this.nowImageHead = nowImageHead;
    }

    public byte[] getNowImage() {
        return nowImage;
    }

    public void setNowImage(byte[] nowImage) {
        this.nowImage = nowImage;
    }

    public byte[] getNowImageHead() {
        return nowImageHead;
    }

    public void setNowImageHead(byte[] nowImageHead) {
        this.nowImageHead = nowImageHead;
    }
}

package com.facecompare.zhumu.util;

public class NdkLoader {
    static {
        System.loadLibrary("yuvutil");
    }

    public static native byte[] rotateYUVDegree90(byte data[], int imageWidth, int imageHeight);

    public static native byte[] rotateYUVDegree270(byte data[], int imageWidth, int imageHeight);

    public static native int calYuvBrightness(byte[] data, int previewWidth, int previewHeight);

    public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight){
        byte[] yuv = new byte[imageWidth*imageHeight*3/2];
        int i =0;int count =0;
        for(i = imageWidth * imageHeight -1; i >=0; i--){
            yuv[count]= data[i];
            count++;
        }
        for(i = imageWidth * imageHeight *3/2-1; i >= imageWidth * imageHeight; i -=2){
            yuv[count++]= data[i -1];
            yuv[count++]= data[i];
        }
        return yuv;
    }
}

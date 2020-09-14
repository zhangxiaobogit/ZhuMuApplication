package com.facecompare.zhumu.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;

import com.facecompare.zhumu.common.utilbean.NowImages;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {

    /**
     * 缩放图片
     *
     * @param bitmap
     * @param w
     * @param h
     * @return
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidht = ((float) w / width);
        float scaleHeight = ((float) h / height);
        /*
         * 通过Matrix类的postScale方法进行缩放
         */
        matrix.postScale(scaleWidht, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newbmp;
    }

    /**
     * 设置图片为圆角
     *
     * @param bitmap
     * @param roundPx 圆角角度
     * @return
     */
    public static Bitmap setRoundedCorner(Bitmap bitmap, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        /*
         * 椭圆形
         */
        final RectF rectF = new RectF(rect);
        /*
         * 去锯齿
         */
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        /*
         * 绘制圆角矩形
         */
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        /*
         * 设置两个图形相交
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private static BitmapFactory.Options newOpts;
    private static ByteArrayOutputStream nowImageBaos;
    private static ByteArrayOutputStream headImageBaos;
    private static YuvImage yuvimage;

    public static NowImages yuv2RgbBytes(byte[] frame, int width, int height, Rect rect) {
        try {
            if (newOpts == null) {
                newOpts = new BitmapFactory.Options();
            }
            if (nowImageBaos == null) {
                nowImageBaos = new ByteArrayOutputStream();
            }
            if (headImageBaos == null) {
                headImageBaos = new ByteArrayOutputStream();
            }
            newOpts.inJustDecodeBounds = true;
            yuvimage = new YuvImage(frame, ImageFormat.NV21, width, height, null);

            nowImageBaos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, nowImageBaos);
            byte[] nowImage = nowImageBaos.toByteArray();
            headImageBaos = new ByteArrayOutputStream();
            int left = rect.left - rect.width() / 2;
            int right = rect.right + rect.width() / 2;
            int top = rect.top - rect.height() / 2;
            int bottom = rect.bottom + rect.height() / 2;
            if (left < 0)
                left = 0;
            if (right > width)
                right = width;
            if (top < 0)
                top = 0;
            if (bottom > height)
                bottom = height;
            yuvimage.compressToJpeg(new Rect(left, top, right, bottom), 100, headImageBaos);
            byte[] headImage = headImageBaos.toByteArray();
            yuvimage = null;
            return new NowImages(nowImage, headImage);
        } catch (Exception e) {
            yuvimage = null;
            e.printStackTrace();
        }
        return null;
    }

    public static void saveIdCardImage(String path, Bitmap bitmap) {
        File file = new File(path);
        if (!file.exists()) {
            ImageUtil.saveBitmapFile(bitmap, file);
        }
    }

    public static void saveBitmapFile(Bitmap bitmap, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            fileOutputStream.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap yuv2Rgb(byte[] frame, int width, int height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                frame,
                ImageFormat.NV21,
                width,
                height,
                null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new android.graphics.Rect(0, 0, width, height), 100, baos);
        byte[] rawImage = baos.toByteArray();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bmp = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        return bmp;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 50, baos);
        return baos.toByteArray();
    }
}

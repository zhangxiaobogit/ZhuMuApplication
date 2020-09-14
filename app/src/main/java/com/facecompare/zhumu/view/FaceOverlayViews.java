package com.facecompare.zhumu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;


import java.util.List;


public class FaceOverlayViews extends View {

    private Paint mPaint;
    private Paint textPaint;
    private String text = "";
    Rect textRect;
    private Paint linePaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private List<Rect> faceRectList;
    private boolean isFront = false;
    private float leftX;
    private float rightX;
    private float leftY;
    private float rightY;
    private float lineYOffset = 0;
    private boolean isComparing = false;


    public FaceOverlayViews(Context context) {
        this(context, null);

    }


    public FaceOverlayViews(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public FaceOverlayViews(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4.0f);
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26, metrics);
        textPaint.setTextSize(size);
        textPaint.setColor(Color.RED);
        textPaint.setStyle(Paint.Style.FILL);
        textRect = new Rect();

        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(stroke);
        linePaint.setStrokeWidth(4.0f);
    }

    public void setFaceQualityColor(boolean flag, String info) {
        text = info;
        if (flag) {
            mPaint.setColor(Color.GREEN);
            linePaint.setColor(Color.GREEN);
        } else {
            mPaint.setColor(Color.RED);
            linePaint.setColor(Color.RED);
        }
//        invalidate();
    }


    public void setComparing(boolean comparing) {
        isComparing = comparing;
        if (comparing) {
            lineYOffset = 0;
        }
    }

    public void setFaces(List<Rect> face) {
        faceRectList = face;
        postInvalidate();
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        {
//            boolean horizontalFlip = SettingUtil.getHorizontalFlip();
//            boolean verticalFlip = SettingUtil.getVerticalFlip();
//            int x = 1, y = 1;
//            if (!horizontalFlip) {
//                x = 1;
//            } else {
//                x = -1;
//            }
//            if (!verticalFlip) {
//                y = 1;
//            } else {
//                y = -1;
//            }
//            canvas.scale(-1, 1, getWidth() / 2, getHeight() / 2);
//        }
//                        LogUtils.e("zxb", "系统版本 ---" + Build.FINGERPRINT);

//        壁挂   Android/rk3288/rk3288:5.1.1/LMY49F/server0601171943:userdebug/test-keys
//        平板   Android/rk3288/rk3288:7.1.2/NHG47K/harris05301421:userdebug/test-keys
//        半圆炮筒  Android/rk3288/rk3288:7.1.2/NHG47K/harris05301506:userdebug/test-keys


        try {

            if (faceRectList != null && faceRectList.size() > 0) {
                { //永惠炮筒注释，其他打开，
                    if (Build.FINGERPRINT.contains("harris04021111") || Build.FINGERPRINT.contains("harris05301506")) {
                        int x = -1, y = 1;
                        canvas.scale(x, y, getWidth() / 2, getHeight() / 2);
                    }
                    if ((mDisplayOrientation == 90 || mDisplayOrientation == 270) && !Build.FINGERPRINT.contains("harris05301506")) {
                        int x = -1;
                        canvas.scale(x, 1, getWidth() / 2, getHeight() / 2);
                    }
                }
                float scaleX = (float) getWidth() / (float) previewWidth;
                float scaleY = (float) getHeight() / (float) previewHeight;
                switch (mDisplayOrientation) {
                    case 90:
                    case 270:
                        scaleX = (float) getWidth() / (float) previewHeight;
                        scaleY = (float) getHeight() / (float) previewWidth;
                        break;
                }

                canvas.rotate(-mOrientation);
                for (int i = 0; i < faceRectList.size(); ++i) {
                    Rect faceRect = faceRectList.get(i);
                    int x = faceRect.left;
                    int y = faceRect.bottom;
                    int imageW = faceRect.width();
                    int imageH = faceRect.height();
                    if (isFront) {
                        if (mDisplayOrientation == 90 || mDisplayOrientation == 270) {
                            x = faceRect.top;
                            y = faceRect.right;
                            leftX = x * scaleX;
                            rightX = (x + imageH) * scaleX;
                        } else {
                            rightX = (x + imageH) * scaleX;
                            leftX = x * scaleX;
                        }
                    } else {
                        leftX = x * scaleX;
                        rightX = (x + imageH) * scaleX;
                    }
                    leftY = (y - imageW) * scaleY;
                    rightY = y * scaleY;
                    float unit = Math.abs(rightX - leftX) * 0.25f;

                    canvas.drawLine(leftX, leftY, leftX + unit, leftY, mPaint);
                    canvas.drawLine(leftX, leftY, leftX, leftY + unit, mPaint);
                    canvas.drawLine(leftX, rightY, leftX + unit, rightY, mPaint);
                    canvas.drawLine(leftX, rightY, leftX, rightY - unit, mPaint);
                    canvas.drawLine(rightX, leftY, rightX - unit, leftY, mPaint);
                    canvas.drawLine(rightX, leftY, rightX, leftY + unit, mPaint);
                    canvas.drawLine(rightX, rightY, rightX - unit, rightY, mPaint);
                    canvas.drawLine(rightX, rightY, rightX, rightY - unit, mPaint);

//                canvas.drawRect(leftX, leftY, rightX, rightY, mPaint);

                    lineYOffset = lineYOffset + (float) Math.abs(rightY - leftY) / 8f;
                    float lineY = leftY + lineYOffset;
                    if (lineYOffset >= rightY - leftY) {
                        lineYOffset = 0;
                    }
                    if (isComparing) {
                        canvas.drawLine(leftX + 5, lineY, rightX - 5, lineY, linePaint);
                    }
                    if (!text.isEmpty()) {
                        textPaint.getTextBounds(text, 0, text.length(), textRect);
                        int rectWidth = (int) Math.abs(rightX - leftX);
                        int textHeight = Math.abs(textRect.bottom - textRect.top) + 5;
                        int textWidth = Math.abs(textRect.right - textRect.left);
                        int offset = Math.abs((rectWidth - textWidth) / 2);
                        if (textWidth <= rectWidth) {
                            canvas.drawText(text, leftX + offset, rightY + textHeight, textPaint);
                        } else {
                            canvas.drawText(text, leftX - offset, rightY + textHeight, textPaint);
                        }
                    }

                }


            }
        } catch (Exception e) {
            Log.e("zxb", "run->  追踪框异常 ：" + e.toString());
        }


    }


    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setFront(boolean front) {
        isFront = front;
    }
}


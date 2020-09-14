package com.facecompare.zhumu.util;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IDesigner on 2017/4/20.
 */

public class CameraUtil {
    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;

    public static final int VERY_HIGH_PHOTO_SIZE = 1;
    public static final int HIGH_PHOTO_SIZE = 2;
    public static final int MEDIUM_PHOTO_SIZE = 3;
    public static final int LOW_PHOTO_SIZE = 4;

    public static Camera.Size findBestPreviewResolution(Camera.Parameters cameraParameters, int screenWidth, int screenHeight) {
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        for (Camera.Size s : rawSupportedSizes) {
//            Log.i("zxb", "===cam:" + s.width + " " + s.height);
        }
        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();
        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {
            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height)
                    .append(' ');
        }


        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenWidth
                / (double) screenHeight;
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然preview宽高比后在比较
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == screenWidth
                    && maybeFlippedHeight == screenHeight) {
                return supportedPreviewResolution;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            return largestPreview;
        }

        // 没有找到合适的，就返回默认的

        return defaultPreviewResolution;
    }


    //自动匹配最佳照片尺寸
    public static Camera.Size findBestPictureResolution(Camera.Parameters cameraParameters, int screenWidth, int screenHeight, int quality) {
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值
        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });
        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenWidth
                / (double) screenHeight;
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            switch (quality) {
                case VERY_HIGH_PHOTO_SIZE:
                    return sortedSupportedPicResolutions.get(0);
                case HIGH_PHOTO_SIZE:
                    return sortedSupportedPicResolutions.get((int) (((double) sortedSupportedPicResolutions.size()) * 0.7));
                case MEDIUM_PHOTO_SIZE:
                    return sortedSupportedPicResolutions.get((int) (((double) sortedSupportedPicResolutions.size()) * 0.9));
                case LOW_PHOTO_SIZE:
                    return sortedSupportedPicResolutions.get(sortedSupportedPicResolutions.size() - 1);
            }
//            return sortedSupportedPicResolutions.get(0);
        }

        // 没有找到合适的，就返回默认的
        return defaultPictureResolution;
    }




    public static boolean is43(double a, double b) {
//        double f = 4.0 / 3;
//        BigDecimal big = new BigDecimal(f);
//        double correct = big.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        double correct = 1.33;
        if (a > b) {
            double temp = a / b;
            BigDecimal big2 = new BigDecimal(temp);
            temp = big2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (temp == correct) {
                return true;
            } else {
                return false;
            }
        } else {
            double temp = b / a;
            BigDecimal big2 = new BigDecimal(temp);
            temp = big2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (temp == correct) {
                return true;
            } else {
                return false;
            }
        }
    }

    //    public static Camera.Size chooseSizeByAspectRatio(List<Camera.Size> choices, int textureViewWidth,
//                                                      int textureViewHeight) {
//        List<Camera.Size> fitAspectRatio = new ArrayList<>();
//        List<Camera.Size> bigEnough = new ArrayList<>();
//        for (Camera.Size option : choices) {
//            if (is43(option.width, option.height)) {
//                fitAspectRatio.add(option);
//            }
//        }
//        if (fitAspectRatio.size() > 0) {
//            for (Camera.Size option : fitAspectRatio) {
//                if ((option.height * option.width) > textureViewHeight * textureViewWidth) {
//                    bigEnough.add(option);
//                }
//            }
//        } else {
//            Collections.max(choices, new CompareSizesByArea());
//            return choices.get(0);
//        }
//        if (bigEnough.size() > 0) {
//            Collections.min(bigEnough, new CompareSizesByArea());
//            return bigEnough.get(0);
//        } else {
//            Collections.max(fitAspectRatio, new CompareSizesByArea());
//            return fitAspectRatio.get(0);
//        }
//    }
    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    public static int findNearestPhotoSize(List<Camera.Size> supportedPreviewResolutions, int width, int height) {
        int nearNum = width * height;
        int diffNum = 9999999;
        int resultIndex = 0;
        for (int i = 0; i < supportedPreviewResolutions.size(); i++) {
            Camera.Size size = supportedPreviewResolutions.get(i);
            int diffNumTemp = Math.abs(size.height * size.width - nearNum);
            if (diffNumTemp < diffNum) {
                diffNum = diffNumTemp;
                resultIndex = i;
            }
        }
        return resultIndex;
    }

    public static Camera.Size findNearest43Resolution(Camera.Parameters cameraParameters, int width, int height) {
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();
        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }
        List<Camera.Size> size43List = new ArrayList<>();
        for (Camera.Size s : rawSupportedSizes) {
            Log.i("zxb", "strart->  ===cam:" + s.width + " " + s.height);
            if (is43(s.width, s.height)) {
                size43List.add(s);
            }
        }
        int nearestIndex = findNearestPhotoSize(size43List, width, height);
        return size43List.get(nearestIndex);
    }

}

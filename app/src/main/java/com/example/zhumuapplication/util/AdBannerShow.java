package com.example.zhumuapplication.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019-1-26.
 */

public class AdBannerShow {
    public static AdBannerShow instance = null;

    private AdBannerShow() {

    }

    public void initAdBanner(Banner banner) {
        List<Object> adImages = new ArrayList<>();
//        if (SpUtils.getAdvertList() != null && SpUtils.getAdvertList().size() > 0) {
//            adImages.add(R.mipmap.bg_attend);
//            adImages.add(R.mipmap.bg_attend);
//            adImages.add(R.mipmap.example001);
//            adImages.add(R.mipmap.example002);
//            adImages.add(R.mipmap.example003);
//            adImages.addAll(SpUtils.getAdvertList());
//        } else {
//            adImages.add(R.mipmap.bg_attend);
//        }
        adImages.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1772686008,2192011385&fm=26&gp=0.jpg");
        adImages.add("https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=3054647180,4290440330&fm=26&gp=0.jpg");
        adImages.add("https://ss1.bdstatic.com/70cFvXSh_Q1YnxGkpoWK1HF6hhy/it/u=2202365617,3927520960&fm=15&gp=0.jpg");
        adImages.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=154658525,993699414&fm=26&gp=0.jpg");
        adImages.add("https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1244970943,2031274636&fm=26&gp=0.jpg");
        if (adImages.size() > 0) {
            AdBannerShow.getInstance().showAdvertisement(banner, adImages);
        }
    }

    public static final AdBannerShow getInstance() {
        if (instance == null) {
            instance = new AdBannerShow();
        }
        return instance;
    }

    public void showAdvertisement(Banner banner, List<Object> images) {

        //设置banner动画效果
        banner.setBannerAnimation(Transformer.DepthPage);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置轮播时间
        banner.setDelayTime(3 * 1000);
        //设置图片集合
        banner.setImages(images);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        banner.setBannerStyle(BannerConfig.NOT_INDICATOR);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    private class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            //Picasso 加载图片简单用法
            if (path instanceof Integer) {
                Glide.with(context).load((int) path).into(imageView);
            } else {
                Glide.with(context).load((String) path).into(imageView);
            }

        }

    }
}

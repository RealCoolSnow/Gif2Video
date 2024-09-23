package com.coolsnow.gif2video.util;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

//import com.coolsnow.gif2video.BuildConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.orhanobut.logger.Logger;

/**
 * File: AdmobHelper
 * Author: CoolSnow(coolsnow2020@gmail.com)
 * Created at: 2021/9/27 16:31
 * Description:
 */
public class AdHelper {
    private static final boolean IS_TEST = false;//BuildConfig.DEBUG;
    /**
     * app id: ca-app-pub-9994404165340290~2261077193
     * ca-app-pub-3940256099942544~3347511713(test)
     * 插页式广告: ca-app-pub-9994404165340290/8303741368
     * ca-app-pub-3940256099942544/1033173712(test)
     * 横幅广告: ca-app-pub-9994404165340290/2329020645
     * ca-app-pub-3940256099942544/6300978111(test)
     */
    private AdRequest adRequest;
    private InterstitialAd interstitialAd;
    private boolean isIniting = false;
    private AdHelper() {
        RequestConfiguration config = new RequestConfiguration.Builder().setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE).build();
        MobileAds.setRequestConfiguration(config);
        adRequest = new AdRequest.Builder().build();
    }

    public static AdHelper getInstance() {
        return Holder.instance;
    }

    private String getPopAdId() {
        return IS_TEST ? "ca-app-pub-3940256099942544/1033173712" : "ca-app-pub-9994404165340290/8303741368";
    }

    private String getBannerId() {
        return IS_TEST ? "ca-app-pub-3940256099942544/6300978111" : "ca-app-pub-9994404165340290/2329020645";
    }

    public String getAppOpenId() {
        return IS_TEST ? "ca-app-pub-3940256099942544/9257395921" : "ca-app-pub-9994404165340290/4199245900";
    }

    public void init(Context context) {
        if(isIniting) {
            return;
        }
        isIniting = true;
        MobileAds.initialize(context, initializationStatus -> {
            Logger.d("init: " + initializationStatus);
            //加载广告
            isIniting = false;
            this.loadInterstitialAd(context);
        });
    }

    private void loadInterstitialAd(Context context) {
        InterstitialAd.load(context, getPopAdId(), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        AdHelper.this.interstitialAd = interstitialAd;
                        Logger.d("loadInterstitialAd - onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        AdHelper.this.interstitialAd = null;
                        Logger.d("onAdFailedToLoad - onAdLoaded:" + loadAdError);
                    }
                });
    }

    public void showInterstitialAd(Activity activity) {
        if (interstitialAd != null) {
            interstitialAd.show(activity);
        } else {
            Logger.d("The interstitial ad wasn't ready yet.");
        }
    }

    public void addAdView(Activity activity, ViewGroup container) {
        AdView adView = new AdView(activity);
        adView.setAdSize(getAdSize(activity));
        adView.setAdUnitId(getBannerId());
        container.addView(adView);
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    private static final class Holder {
        private static AdHelper instance = new AdHelper();
    }
}

package com.coolsnow.gif2video;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/**
 * Created by coolsnow on 2020/3/24.
 */
public class Application extends android.app.Application {
    private static Application _Application;

    public static Application getInstance() {
        return _Application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _Application = this;
        initUmeng();
        if (Constant.DEBUG) {
            Logger.addLogAdapter(new AndroidLogAdapter());
        }
        Logger.d("onCreate");
    }

    private void initUmeng() {
        UMConfigure.init(this, Constant.UMENG_KEY, "google", UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        UMConfigure.setLogEnabled(Constant.DEBUG);
    }
}

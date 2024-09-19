package com.coolsnow.gif2video.activity.base;

import androidx.appcompat.app.AppCompatActivity;

//import com.umeng.analytics.MobclickAgent;

/**
 * Created by coolsnow on 2020/3/24.
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
//        MobclickAgent.onPause(this);
        super.onPause();
    }
}

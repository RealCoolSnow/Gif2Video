package com.coolsnow.gif2video.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import com.coolsnow.gif2video.R;
import com.coolsnow.gif2video.activity.base.BaseActivity;
import com.coolsnow.gif2video.databinding.ActivityMainBinding;
import com.coolsnow.gif2video.network.NetworkAPI;
import com.coolsnow.gif2video.util.AdHelper;
import com.coolsnow.gif2video.util.Dialogs;
import com.coolsnow.gif2video.util.UpdateInfo;
import com.coolsnow.gif2video.util.Util;
import com.coolsnow.gif2video.util.VersionUtil;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ImagePickerConfig;
import com.esafirm.imagepicker.features.ImagePickerFragment;
import com.esafirm.imagepicker.features.ImagePickerInteractionListener;
import com.esafirm.imagepicker.features.IpCons;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by coolsnow on 2020/3/24.
 */
public class MainActivity extends BaseActivity implements ImagePickerInteractionListener {
    private ActivityMainBinding binding;
    private ImagePickerFragment imagePickerFragment;
    private ImagePickerConfig config;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (savedInstanceState != null) {
            // The fragment has been restored.
            imagePickerFragment = (ImagePickerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
        } else {
            config = new ImagePickerConfig();
            config.setOnlyAnimation(true);
            config.setIncludeAnimation(true);
            config.setReturnMode(ReturnMode.GALLERY_ONLY);
            config.setMode(IpCons.MODE_SINGLE);
            imagePickerFragment = ImagePickerFragment.newInstance(config, null);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_placeholder, imagePickerFragment);
            ft.commit();
        }
        initView();
//        checkUpdate();
        //loadAd();
        AdHelper.getInstance().init(this);
        AdHelper.getInstance().addAdView(this, binding.adContainer);
    }

//    private void loadAd() {
//        MobileAds.initialize(this, initializationStatus -> {
//        });
//        AdRequest adRequest = new AdRequest.Builder().build();
//        binding.adView.loadAd(adRequest);
//    }

    private void initView() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            imagePickerFragment.getData();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            }, 800);
        });
        binding.tvUsage.setOnClickListener(v -> Util.open(MainActivity.this, getString(R.string.feedback_link)));
    }

    private void checkUpdate() {
        NetworkAPI.getInstance().checkUpdate(new NetworkAPI.NetworkCallback() {
            @Override
            public void onError(int errno, String errmsg) {
                Logger.d(errmsg);
            }

            @Override
            public void onSuccess(int code, String content) {
                MainActivity.this.runOnUiThread(() -> _checkUpdate(content));

            }
        });
    }

    private void _checkUpdate(String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            if (jsonObject.has("data")) {
                UpdateInfo updateInfo = new UpdateInfo(MainActivity.this, jsonObject.optString("data"));
                if (updateInfo.isNeedUpdate()) {
                    Dialogs.showMessage(MainActivity.this, getString(R.string.hint), updateInfo.hint, getString(R.string.update),
                            (dialog, which) -> Util.open(MainActivity.this, updateInfo.url), getString(R.string.cancel), null);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPickImage(View view) {
        Toast.makeText(this, "pick", Toast.LENGTH_SHORT).show();
    }

    private void showAbout() {
        Dialogs.showMessage(this, getString(R.string.about), getString(R.string.about_text) + VersionUtil.getVersionName(this), getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, null, null);
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void finishPickImages(Intent result) {
        ArrayList<Image> images = (ArrayList<Image>) ImagePicker.getImages(result);
        if (images != null && images.size() > 0) {
            ConvertActivity.show(this, images.get(0).getPath());
        }
    }

    @Override
    public void selectionChanged(List<Image> imageList) {
        //Toast.makeText(this, "finishPickImages", Toast.LENGTH_SHORT).show();
    }
}

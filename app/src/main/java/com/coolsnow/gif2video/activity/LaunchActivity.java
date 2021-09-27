package com.coolsnow.gif2video.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coolsnow.gif2video.R;
import com.coolsnow.gif2video.activity.base.BaseActivity;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import tool.easypermissions.AfterPermissionGranted;
import tool.easypermissions.EasyPermissions;

import static tool.easypermissions.EasyPermissions.hasPermissions;

/**
 * Created by coolsnow on 2020/3/25.
 */
public class LaunchActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "LaunchActivity";
    private static final int RC_ALL_PERMISSIONS = 2000;
    private int waitPermissionCount = 0;
    private boolean finished = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestPermission();
        showMain();
    }

    @AfterPermissionGranted(RC_ALL_PERMISSIONS)
    private void requestPermission() {
        List<String> permList = new ArrayList<>();
        if (!hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permList.size() > 0) {
            String[] perms = permList.toArray(new String[permList.size()]);
            EasyPermissions.requestPermissions(this, "",
                    RC_ALL_PERMISSIONS, perms);
            Logger.d(TAG, "requestPermissions -> " + perms.length);
            waitPermissionCount = permList.size();
        } else {
            showMain();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        Logger.d(TAG, "onRequestPermissionsResult->" + requestCode);
        waitPermissionCount -= permissions.length;
        if (waitPermissionCount <= 0 && !finished) {
            showMain();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (RC_ALL_PERMISSIONS == requestCode && perms.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, getString(R.string.storage_permission_denied), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void showMain() {
        startActivity(new Intent(this, MainActivity.class));
        finished = true;
        finish();
    }
}

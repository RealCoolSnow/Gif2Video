package com.coolsnow.gif2video.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by coolsnow on 2020/3/25.
 */
public class Util {
    public static void open(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String timeStamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }

    public static String sysInfo() {
        return Build.MANUFACTURER + "," + Build.MODEL + ",API-" + Build.VERSION.SDK_INT;
    }
}

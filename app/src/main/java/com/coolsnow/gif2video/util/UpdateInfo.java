package com.coolsnow.gif2video.util;

import android.content.Context;
import android.text.TextUtils;

import com.coolsnow.gif2video.Application;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by coolsnow on 2017/3/17.
 */

public class UpdateInfo {
    public int vcode;
    public String vname;
    public String url;
    public String hint;

    /*
    {"vcode":100,"vname":"1.0.1","url":"http://www.baidu.com","hint":"新版本,请立即升级"}
     */
    public UpdateInfo(Context context, String text) {
        if (!TextUtils.isEmpty(text)) {
            try {
                JSONObject jsonObject = new JSONObject(text);
                if (jsonObject.has("vcode")) {
                    vcode = jsonObject.optInt("vcode");
                }
                if (jsonObject.has("vname")) {
                    vname = jsonObject.optString("vname");
                }
                if (jsonObject.has("url")) {
                    url = jsonObject.optString("url");
                }
                if (jsonObject.has("hint")) {
                    hint = jsonObject.optString("hint");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isNeedUpdate() {
        int v = VersionUtil.getVersionCode(Application.getInstance());
        if (v > 0 && vcode > 0 && vcode > v) {
            return true;
        }
        return false;
    }
}

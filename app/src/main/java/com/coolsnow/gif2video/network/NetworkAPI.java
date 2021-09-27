package com.coolsnow.gif2video.network;


import android.text.TextUtils;

import com.coolsnow.gif2video.Application;
import com.coolsnow.gif2video.Constant;
import com.coolsnow.gif2video.util.VersionUtil;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dangxingdong on 2016/12/29.
 */

public class NetworkAPI {

    public static final String TAG = "NetworkAPI";
    public static final int CONNECT_TIMEOUT = 20;
    public static final int READ_TIMEOUT = 20;
    public static final int WRITE_TIMEOUT = 20;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //private static final String PARAM_VERSION = "1.0.0";
    private OkHttpClient mOkHttpClient;

    private String appVersion = "";

    private static String[] VERIFY_HOST_NAME_ARRAY = new String[]{"www.joy666.cn", "joy666.cn"};

    private static class Holder {
        private static NetworkAPI instance = new NetworkAPI();
    }

    public static NetworkAPI getInstance() {
        return Holder.instance;
    }

    private NetworkAPI() {
        mOkHttpClient = new OkHttpClient.Builder()
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> {
                    if (TextUtils.isEmpty(hostname)) {
                        return false;
                    }
                    return Arrays.asList(VERIFY_HOST_NAME_ARRAY).contains(hostname);
                })
                .build();
        appVersion = VersionUtil.getVersionName(Application.getInstance());
    }

    public void checkUpdate(NetworkCallback callback) {
        FormBody formBody = new FormBody.Builder()
                .add("version", appVersion)
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL_API + "/gif2mp4_update")
                .post(formBody)
                .build();
        exeRequest(request, new CallbackProxy(callback));
    }

    private void exeRequest(Request request, Callback callback) {
        Logger.d(TAG, "request:" + request.toString());
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    class CallbackProxy implements Callback {
        NetworkCallback mCallback;

        public CallbackProxy(NetworkCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Logger.d(TAG, "onFailure:" + e.toString());
            if (mCallback != null) {
                mCallback.onError(-1, e.toString());
            }
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String content = response.body() != null ? response.body().string() : "";
            Logger.d(TAG, "onResponse:" + content);
            if (mCallback != null) {
                mCallback.onSuccess(response.code(), content);
            }
        }
    }

    public interface NetworkCallback {
        void onError(int errno, String errmsg);

        void onSuccess(int code, String content);
    }
}

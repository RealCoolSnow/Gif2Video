package com.coolsnow.gif2video.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.coolsnow.gif2video.R;
import com.coolsnow.gif2video.activity.base.BaseActivity;
import com.coolsnow.gif2video.databinding.ActivityConvertBinding;
import com.coolsnow.gif2video.util.AdHelper;
import com.coolsnow.gif2video.util.Dialogs;
import com.coolsnow.gif2video.util.Loading;
import com.coolsnow.gif2video.util.Util;
import com.esafirm.imagepicker.features.imageloader.DefaultImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.orhanobut.logger.Logger;
import com.otaliastudios.gif.GIFCompressor;
import com.otaliastudios.gif.GIFListener;
import com.otaliastudios.gif.GIFOptions;
import com.otaliastudios.gif.sink.DataSink;
import com.otaliastudios.gif.sink.DefaultDataSink;
import com.otaliastudios.gif.strategy.DefaultStrategy;
import com.otaliastudios.gif.strategy.Strategy;
import com.otaliastudios.gif.strategy.size.AspectRatioResizer;
import com.otaliastudios.gif.strategy.size.FractionResizer;
import com.otaliastudios.gif.strategy.size.PassThroughResizer;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

public class ConvertActivity extends BaseActivity implements GIFListener {
    private static final String EXTRA_FILE_PATH = "file_path";
    private static final int PROGRESS_BAR_MAX = 100;
    private static final String FILE_PROVIDER_AUTHORITY = "com.coolsnow.gif2video.fileprovider";
    private ActivityConvertBinding binding;
    private File mOutputFile;
    private long mStartTime;
    private Strategy mStrategy;
    private boolean mIsCompressing;
    private Future<Void> mCompressionFuture;
    private String mImagePath;

    public static void show(Context context, String filepath) {
        Intent intent = new Intent(context, ConvertActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, filepath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConvertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getIntent() != null) {
            mImagePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        }
        setIsCompressing(false);
        initView();
        syncParameters();
    }

    private void syncParameters() {
        int frames = DefaultStrategy.DEFAULT_FRAME_RATE;
        float fraction = 1F;
        float aspectRatio = 0F;
        mStrategy = new DefaultStrategy.Builder()
                .addResizer(aspectRatio > 0 ? new AspectRatioResizer(aspectRatio) : new PassThroughResizer())
                .addResizer(new FractionResizer(fraction))
                .frameRate(frames)
                .build();
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.convert_to_video);
        new DefaultImageLoader().loadImage(mImagePath, binding.gifImage, ImageType.GALLERY);
    }

    private int getRepeatCount() {
        Object selected = binding.spRepeatCount.getSelectedItem();
        String repeatCount = selected != null ? selected.toString() : "";
        return TextUtils.isEmpty(repeatCount) ? 1 : Integer.parseInt(repeatCount);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onConvert(View view) {
        switch (view.getId()) {
            case R.id.btn_convert:
                convert();
                break;
        }
    }

    private void convert() {
        if (!mIsCompressing) {
            Loading.getInstance().show(this, getString(R.string.converting), false);
            transcode();
        } else {
            mCompressionFuture.cancel(true);
        }
    }

    private void transcode() {
        try {
            File outputDir = new File(getSaveDir());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            mOutputFile = new File(outputDir, Util.timeStamp() + ".mp4");
            if (!mOutputFile.exists()) {
                mOutputFile.createNewFile();
            }
            Logger.i("Transcoding into " + mOutputFile);
        } catch (IOException e) {
            Logger.e("Failed to create temporary file.", e);
            Toast.makeText(this, R.string.convert_fail, Toast.LENGTH_LONG).show();
            Loading.getInstance().dismiss();
            return;
        }
        int rotation = 0;
        float speed = 1F;
        mStartTime = SystemClock.uptimeMillis();
        setIsCompressing(true);
        DataSink sink = new DefaultDataSink(mOutputFile.getAbsolutePath());
        GIFOptions.Builder builder = GIFCompressor.into(sink);
        int repeatCount = getRepeatCount();
        for (int i = 0; i < repeatCount; i++) {
            builder.addDataSource(this, mImagePath);
        }
        mCompressionFuture = builder.setListener(this)
                .setStrategy(mStrategy)
                .setRotation(rotation)
                .setSpeed(speed)
                .compress();
    }

    private void setIsCompressing(boolean isCompressing) {
        mIsCompressing = isCompressing;
        binding.btnConvert.setEnabled(!mIsCompressing);
    }

    @Override
    public void onGIFCompressionProgress(double progress) {
        if (progress > 0) {
            Loading.getInstance().setMessage(getString(R.string.converting) + Math.round(progress * PROGRESS_BAR_MAX) + "%");
        }
    }

    @Override
    public void onGIFCompressionCompleted() {
        Logger.d("Compression took " + (SystemClock.uptimeMillis() - mStartTime) + "ms");
        onCompressionFinished(true, "Compressed video placed on " + mOutputFile);
        showSuccess();
        MobclickAgent.onEvent(this, "convert_success", Util.sysInfo());
    }

    @Override
    public void onGIFCompressionCanceled() {
        onCompressionFinished(false, "GIFCompressor canceled.");
        Toast.makeText(this, R.string.convert_fail, Toast.LENGTH_SHORT).show();
        MobclickAgent.onEvent(this, "convert_cancel", Util.sysInfo());
    }

    @Override
    public void onGIFCompressionFailed(@NonNull Throwable exception) {
        onCompressionFinished(false, "GIFCompressor error occurred. " + exception.getMessage());
        Toast.makeText(this, R.string.convert_fail, Toast.LENGTH_SHORT).show();
        MobclickAgent.onEvent(this, "convert_fail", exception.getMessage() + " " + Util.sysInfo());
    }

    private void onCompressionFinished(boolean isSuccess, @NonNull String toastMessage) {
        setIsCompressing(false);
        Loading.getInstance().dismiss();
    }

    private String getSaveDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "GIF2MP4";
    }

    private void showSuccess() {
        String filePath = mOutputFile.getAbsolutePath();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
        Dialogs.showMessage(this, null, getString(R.string.convert_finish) + filePath, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, null, null);
        new Handler().postDelayed(() -> AdHelper.getInstance().showInterstitialAd(ConvertActivity.this), 800);
    }
}

package com.example.audioandvideo;

import android.Manifest;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.daxiong.commonutil.util.ScreenUtil;
import com.example.libaudio.util.Constant;
import com.example.libvideo.Camera2Record;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;

import io.reactivex.functions.Consumer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/15
 */
public class VideoActivity extends BaseActivity implements TextureView.SurfaceTextureListener, View.OnClickListener{
    private static String TAG = "VideoActivity";
    private TextureView mTextureView;
    private Camera2Record mCamera;
    private Button mBtnRecord, mBtnStop;
    private File mVideoFilePath;
    @Override
    public int getLayout() {
        return R.layout.activity_video;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initview();
        check();
    }

    private void check() {
        RxPermissions rxPermissions = new RxPermissions(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions.requestEach(new String[]{Manifest.permission.CAMERA})
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                // 用户已经同意该权限
                                Log.d(TAG, permission.name + " is granted.");
                                initCamera();
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                Log.d(TAG, permission.name + " is denied. More info should be provided.");
                            } else {
                                // 用户拒绝了该权限，并且选中『不再询问』
                                Log.d(TAG, permission.name + " is denied.");
                            }
                        }
                    });
        }
    }

    private void initview() {

        mBtnRecord = findViewById(R.id.btn_record);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnStop.setOnClickListener(this);
        mBtnRecord.setOnClickListener(this);
        mTextureView = findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(this);
        mCamera = new Camera2Record(this, mTextureView);

    }

    private void initCamera(){
        mCamera.openCamera2(ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this) - ScreenUtil.dip2px(this, 200));
    }

    private void initDir(){
        mVideoFilePath = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.VIDEO_FILE_NAME);
        if(!mVideoFilePath.exists()){
            try {
                mVideoFilePath.createNewFile();
                Log.e(TAG, "mVideoFilePath created success");
                Log.e(TAG, "mAudioFile.path:" + mVideoFilePath.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "mAudioFile created IOException");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.closeCamera();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        mCamera.openCamera2(ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this) - ScreenUtil.dip2px(this, 200));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_stop:
                Toast.makeText(this,"结束录制",Toast.LENGTH_SHORT).show();
                mCamera.stopVideoRecord();
                break;
            case R.id.btn_record:
                initDir();
                Toast.makeText(this,"开始录制",Toast.LENGTH_SHORT).show();
                mCamera.startRecordVideo(mVideoFilePath.getAbsolutePath());
                break;
        }
    }
}

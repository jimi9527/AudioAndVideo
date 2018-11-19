package com.example.audioandvideo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.audioandvideo.opengles.TriangleActivity;
import com.example.libaudio.util.Constant;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;

import io.reactivex.functions.Consumer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/15
 */
public class EntranceActivity extends BaseActivity{
    private final static String TAG = "EntranceActivity";
    @Override
    public int getLayout() {
        return R.layout.activity_entrance;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPermission();
    }

    private void initPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rxPermissions.requestEach(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE})
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                // 用户已经同意该权限
                                Log.d(TAG, permission.name + " is granted.");
                                initDir();
                            } else if (permission.shouldShowRequestPermissionRationale) {
                                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                Log.d(TAG, permission.name + " is denied. More info should be provided.");
                            } else {
                                // 用户拒绝了该权限，并且选中『不再询问』
                                Log.d(TAG, permission.name + " is denied.");
                            }
                        }
                    });
            rxPermissions.requestEach(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO })
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                // 用户已经同意该权限
                                Log.d(TAG, permission.name + " is granted.");
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

    // 初始化目录
    private void initDir(){
        File rootFile = new File(com.example.libaudio.util.Constant.CONTANT_PATH, Constant.AUDIO);
        if(!rootFile.exists()){
            rootFile.mkdirs();
        }
        Log.e(TAG, "initDir success:");
    }

    public void audio(View view){
       toAct(MainActivity.class);
    }

    public void video(View view){
        toAct(VideoActivity.class);
    }

    public void muxer(View view){
        toAct(MuxerActivity.class);
    }

    public void triangle(View view){
        toAct(TriangleActivity.class);
    }

    private void toAct(Class clazz){
        Intent intent = new Intent(EntranceActivity.this, clazz);
        startActivity(intent);
    }
}

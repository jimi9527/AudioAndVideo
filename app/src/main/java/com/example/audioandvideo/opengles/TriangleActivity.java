package com.example.audioandvideo.opengles;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class TriangleActivity extends AppCompatActivity {
    private static final String TAG = "TriangleActivity";
    private static final int MESSAGE_FINISH = 1;
    private GLSurfaceView mGLSurfaceView;
    private boolean rendererset;
    private Bitmap mBitmap;
    private ImageRenderer imageRenderer;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_FINISH:
                    Log.d(TAG, "MESSAGE_FINISH");

                    break;

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);


        if (checkOpenGL2()) {
            mGLSurfaceView.setEGLContextClientVersion(2);
            //mGLSurfaceView.setRenderer(new TriangleRenderer(this));
            setImageRemderer();
            rendererset = true;
        }

        // new BitmapThread().start();
    }

    // 设置图片渲染器
    private void setImageRemderer() {
        imageRenderer = new ImageRenderer(this);
        mGLSurfaceView.setRenderer(imageRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    private boolean checkOpenGL2() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsES2 = configurationInfo.reqGlEsVersion >= 0x20000;
        return supportsES2;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererset && mGLSurfaceView != null) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererset && mGLSurfaceView != null) {
            Log.d(TAG, "mGLSurfaceView:" + mGLSurfaceView);
            mGLSurfaceView.onResume();
        }
    }
}

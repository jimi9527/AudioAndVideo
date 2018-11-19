package com.example.audioandvideo.opengles;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.print.PrinterId;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class TriangleActivity extends AppCompatActivity{
    private GLSurfaceView mGLSurfaceView;
    private boolean rendererset;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);

        if(checkOpenGL2()){
            mGLSurfaceView.setEGLContextClientVersion(2);
            mGLSurfaceView.setRenderer(new TriangleRenderer());
            rendererset = true;
        }
    }


    private boolean checkOpenGL2(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsES2 = configurationInfo.reqGlEsVersion >= 0x20000;
        return supportsES2;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererset){
            mGLSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(rendererset){
            mGLSurfaceView.onResume();
        }
    }
}

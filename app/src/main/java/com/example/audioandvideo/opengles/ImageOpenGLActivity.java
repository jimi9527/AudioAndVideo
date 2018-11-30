package com.example.audioandvideo.opengles;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.audioandvideo.R;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/20
 */
public class ImageOpenGLActivity extends AppCompatActivity{
    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);

        setContentView(mGLSurfaceView);
    }
}

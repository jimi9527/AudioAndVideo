package com.example.audioandvideo.opengles;

import android.opengl.GLSurfaceView;
import android.print.PrinterId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class TriangleRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOATS = 4;
    private FloatBuffer vertexData = null;
    // 三角形的各个顶点
    private float[] triangleWithVertexData = {
            0f, 0f,
            9f, 14f,
            0f, 14f
    };




    public TriangleRenderer() {
        vertexData = ByteBuffer.allocateDirect(triangleWithVertexData.length * BYTES_PER_FLOATS)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(triangleWithVertexData);


    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        gl10.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        gl10.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
    }
}

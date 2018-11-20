package com.example.audioandvideo.opengles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.print.PrinterId;
import android.util.Log;

import com.example.audioandvideo.opengles.util.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import static android.opengl.GLES20.glUseProgram;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/20
 */
public class ImageRenderer implements GLSurfaceView.Renderer{
    private final static String TAG = "ImageRenderer";
    private String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vCoordinate;" +
                    "uniform mat4 vMatrix;" +
                    "varying vec2 aCoordinate;" +
                    "void main(){" +
                    "gl_Position = vMatrix * vPosition;" +
                    "aCoordinate = vCoordinate;"
            + "}";
    private String fragemntShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D vTexture;" +
                    "varying vec2 aCoordinate;" +
                    "void main(){" +
                    " gl_FragColor=texture2D(vTexture,aCoordinate);"
                    + "}";
    private final float[] vertexPos={
            -1.0f,1.0f,    //左上角
            -1.0f,-1.0f,   //左下角
            1.0f,1.0f,     //右上角
            1.0f,-1.0f     //右下角
    };

    private final float[] sCoord={
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,0.0f,
            1.0f,1.0f,
    };


    private Context context;
    private FloatBuffer vertexData;
    private static final int BYTES_PER_FLOATS = 4;
    // opengl程序对象ID
    private int programId;

    public ImageRenderer(Context context) {
        this.context = context;

        vertexData = ByteBuffer.allocateDirect(vertexPos.length * BYTES_PER_FLOATS)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertexPos);
        vertexData.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        Log.d(TAG, "vertexShaderSource:" +vertexShaderCode);
        Log.d(TAG, "fragmentShaderSource:" +fragemntShaderCode);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragemntShaderCode);

         programId = ShaderHelper.linkProgram(vertexShader, fragmentShader);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        gl10.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);

        if(ShaderHelper.validateProgram(programId)){
            glUseProgram(programId);
            
        }

    }
}

package com.example.audioandvideo.opengles;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.audioandvideo.opengles.util.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class TriangleRenderer implements GLSurfaceView.Renderer {
    private final static String TAG = "TriangleRenderer";
    private static final String U_COLOR = "u_Color";
    private int uColorLocation;
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOATS = 4;
    private FloatBuffer vertexData = null;
    // 三角形的各个顶点
    private float[] triangleWithVertexData = {
            0f, 0f,
            9f, 14f,
            0f, 14f,
            0f, 0f,
            9f, 0f,
            9f, 14f,
    };

    private Context context;
    private final String vertexShaderCode =
            "attribute vec4 a_Position;" +
                    "void main() {" +
                    "  gl_Position = a_Position;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 u_Color;" +
                    "void main() {" +
                    "  gl_FragColor = u_Color;" +
                    "}";

    public TriangleRenderer(Context context) {
        this.context = context;
        vertexData = ByteBuffer.allocateDirect(triangleWithVertexData.length * BYTES_PER_FLOATS)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(triangleWithVertexData);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        //String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.triangle_fragment_shader);
        //String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.triangle_vertex_shader);

        Log.d(TAG, "vertexShaderSource:" +vertexShaderCode);
        Log.d(TAG, "fragmentShaderSource:" +fragmentShaderCode);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderCode);

        int programId = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if(ShaderHelper.validateProgram(programId)){
            glUseProgram(programId);
            // 获取uniform的位置
            uColorLocation = glGetUniformLocation(programId, U_COLOR);
            // 获取属性的位置
            aPositionLocation = glGetAttribLocation(programId, A_POSITION);

            Log.d(TAG, "uColorLocation:" + uColorLocation);
            Log.d(TAG, "aPositionLocation:" + aPositionLocation);

            vertexData.position(0);
            // 在缓冲区的vertexData找到aPositionLocation 对应的数据
            glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);
            glEnableVertexAttribArray(aPositionLocation);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        gl10.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);

        // 更新着色器代码中u_color的值
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        // 绘制两个三角形
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
}

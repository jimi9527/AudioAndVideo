package com.example.audioandvideo.opengles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.audioandvideo.R;
import com.example.audioandvideo.opengles.util.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLUtils.texImage2D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/20
 */
public class ImageRenderer implements GLSurfaceView.Renderer {
    private final static String TAG = "ImageRenderer";
    private String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";
    private String fragemntShaderCode =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(s_texture, v_texCoord);" +
                    "}";
    private static final float[] VERTEX = {   // in counterclockwise order:
            1, 1, 0,   // top right
            -1, 1, 0,  // top left
            -1, -1, 0, // bottom left
            1, -1, 0,  // bottom right
    };
    private static final short[] VERTEX_INDEX = {
            0, 1, 2, 0, 2, 3
    };
    private static final float[] TEX_VERTEX = {   // in clockwise order:
            0.5f, 0,  // bottom right
            0, 0,  // bottom left
            0, 0.5f,  // top left
            0.5f, 0.5f,  // top right
    };

    private Context context;
    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mTexVertexBuffer;
    private final ShortBuffer mVertexIndexBuffer;

    private static final int BYTES_PER_FLOATS = 4;
    private static final int BYTES_PRE_SHORTS = 2;
    // opengl程序对象ID
    private int programId;

    private float[] mMVPMatrix = new float[16];

    private int mPositionHandle;
    private int mMatrixHandle;
    private int mTexCoordHandle;
    private int mTexSamplerHandle;

    public ImageRenderer(Context context) {
        this.context = context;

        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * BYTES_PER_FLOATS)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(VERTEX);
        mVertexBuffer.position(0);

        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * BYTES_PER_FLOATS)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexVertexBuffer.put(TEX_VERTEX);
        mTexVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * BYTES_PRE_SHORTS)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        mVertexIndexBuffer.put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        Log.d(TAG, "vertexShaderSource:" + vertexShaderCode);
        Log.d(TAG, "fragmentShaderSource:" + fragemntShaderCode);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragemntShaderCode);

        programId = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        if (ShaderHelper.validateProgram(programId)) {
            glUseProgram(programId);

            mPositionHandle = glGetAttribLocation(programId, "vPosition");
            mTexCoordHandle = glGetAttribLocation(programId, "a_texCoord");
            mMatrixHandle = glGetUniformLocation(programId, "uMVPMatrix");
            mTexSamplerHandle = glGetUniformLocation(programId, "s_texture");

            glEnableVertexAttribArray(mPositionHandle);
            glVertexAttribPointer(mPositionHandle, 3, GL_FLOAT, false, 12, mVertexBuffer);
            glEnableVertexAttribArray(mTexCoordHandle);
            glVertexAttribPointer(mTexCoordHandle, 2, GL_FLOAT, false, 0, mTexVertexBuffer);
            Log.d(TAG, "mPositionHandle:" + mPositionHandle);
            Log.d(TAG, "mTexCoordHandle:" + mTexCoordHandle);
            Log.d(TAG, "mMatrixHandle:" + mMatrixHandle);
            Log.d(TAG, "mTexSamplerHandle:" + mTexSamplerHandle);

            // int textureId = createTexture();

            Bitmap mBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_index_present);
            int textureID =  createTexture(mBitmap);
            Log.d(TAG, "textureId:" + textureID);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        gl10.glViewport(0, 0, i, i1);


        Matrix.perspectiveM(mMVPMatrix, 0, 45, (float)i / i1, 0.1f, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -5f);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);

        glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        glUniform1i(mTexSamplerHandle, 0);

        glDrawElements(GL_TRIANGLES, VERTEX_INDEX.length, GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }

    private int createTexture(Bitmap mBitmap) {
        int[] texture = new int[1];

        if (mBitmap != null && !mBitmap.isRecycled()) {
            glGenTextures(1, texture, 0);

            if (texture[0] == 0) {
                Log.d(TAG, "create texture object fail");
                glDeleteTextures(1, texture, 0);
                return 0;
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            // 绑定到对应的纹理的对象ID
            glBindTexture(GL_TEXTURE_2D, texture[0]);
            //设置默认的纹理过滤参数
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_REPEAT);
            //读入位图数据到当前绑定的纹理对象
            texImage2D(GL_TEXTURE_2D, 0, mBitmap, 0);
            mBitmap.recycle();
        }

        return texture[0];
    }
}

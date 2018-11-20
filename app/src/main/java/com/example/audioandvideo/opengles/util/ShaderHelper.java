package com.example.audioandvideo.opengles.util;

import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class ShaderHelper {
    private final static String TAG = "ShaderHelper";


    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {

        // 创建着色器对象
        int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            Log.d(TAG, "create shader object fail");
            return 0;
        }
        // 将着色器代码上传到着色器对象里
        glShaderSource(shaderObjectId, shaderCode);
        // 编译着色器
        glCompileShader(shaderObjectId);
        // 检查是否编译成功
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            // 编译失败，删除着色器对象
            Log.d(TAG, "compile shader object fail");
            glDeleteShader(shaderObjectId);
            return 0;
        }

        return shaderObjectId;
    }

    // 将着色器链接到OPenGL程序里
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 创建程序
        int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            Log.d(TAG, "create program object fail");
            return 0;
        }
        // 将顶点着色器和片段着色器附加到程序对象
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        // 链接
        glLinkProgram(programObjectId);
        // 检查链接是否成功
        int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        if(linkStatus[0] == 0){
            // 链接程序对象失败
            Log.d(TAG, "linkprogram object fail");
            glDeleteShader(programObjectId);

            return 0;
        }
        return programObjectId;
    }

    // 验证程序
    public static boolean validateProgram(int programId){
        glValidateProgram(programId);
        int[] validateStatus = new int[1];
        glGetProgramiv(programId, GL_VALIDATE_STATUS, validateStatus, 0);
        Log.d(TAG, "results of validating program:" + validateStatus[0]);
        Log.d(TAG, "validating prgram log:" + glGetProgramInfoLog(programId));

        return validateStatus[0] != 0 ;
    }
}

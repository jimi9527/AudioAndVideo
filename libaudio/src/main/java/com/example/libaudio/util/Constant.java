package com.example.libaudio.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/6
 */
public class Constant {
    // 录音文件目录
    public static File CONTANT_PATH = Environment.getExternalStorageDirectory();
    // 录音文件目录
    public static String AUDIO = "/audio";
    // 录音文件名称
    public static String AUDIO_FILE_NAME = "/test.pcm";
    // wav文件格式
    public static String ADUIO_WAV_FILE_NAME = "/test.wav";
    // 视频文件
    public static String VIDEO_FILE_NAME = "/demo.mp4";
    // 分离出来的视频文件
    public static String OUTPUT_VIDEO = "/outputvideo.mp4";
    // 分离出来的音频文件
    public static String OUTPUT_AUDIO = "/outputaudio.mp3";
    // 合成的视频文件
    public static String COMBIN_FILE = "/combinvideo.mp4";

    // 音频输入源
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    // 音频采样率 44100适合大部分手机
    public static final int AUDIO_SAMPLERATEInHz = 44100;
    // 通道数配置
    public static final int AUDIO_CHANNLE = AudioFormat.CHANNEL_IN_STEREO;
    // 数据位宽 16bit 适合大部分手机
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // 音频管理策略
    public static final int ADUIO_STREAMTYPE = AudioManager.STREAM_MUSIC;

}

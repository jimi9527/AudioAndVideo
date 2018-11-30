package com.example.libvideo.mediac;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/30
 */
public class VideoDecode implements Runnable {
    private String MIME_TYPE = "video/avc";
    // 视频的比特率
    private final static int FORMAT_BITRATE = 125000;
    // 视频的帧率
    private final static int FORMAT_FPS = 30;

    private String mSavePath;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;

    public VideoDecode(String mSavePath) {
        this.mSavePath = mSavePath;
    }

    public void init(int width, int height) {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
            mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, FORMAT_BITRATE);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FORMAT_FPS);
            // 设置关键帧
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            // 设置颜色格式
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        mediaCodec.start();
    }

    public void readData(byte[] h264Data){
        int inputIdex = mediaCodec.dequeueInputBuffer(1000);
        if(inputIdex >= 0){
            ByteBuffer inputBuffer = getInputBuffer(inputIdex);
            inputBuffer.clear();
            inputBuffer.put(h264Data, 0, h264Data.length);
            mediaCodec.queueInputBuffer(inputIdex, 0, h264Data.length, 0, 0);
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
        while (outIndex > 0){
            ByteBuffer outBuffer = getOutputBuffer(outIndex);
            outBuffer.position(0);
            outBuffer.limit(bufferInfo.offset + bufferInfo.size);
            byte[] yuvData = new byte[outBuffer.remaining()];
            outBuffer.get(yuvData);

            mediaCodec.releaseOutputBuffer(outIndex, false);
            outBuffer.clear();
            outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
        }
    }

    public void stop(){
        mediaCodec.release();
        mediaCodec.stop();

    }


    private ByteBuffer getInputBuffer(int index) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mediaCodec.getInputBuffer(index);
        } else {
            return mediaCodec.getInputBuffers()[index];
        }
    }

    private ByteBuffer getOutputBuffer(int index) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mediaCodec.getOutputBuffer(index);
        } else {
            return mediaCodec.getOutputBuffers()[index];
        }
    }

    @Override
    public void run() {

    }
}

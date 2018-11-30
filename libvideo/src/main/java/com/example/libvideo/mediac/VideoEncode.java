package com.example.libvideo.mediac;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/29
 */
public class VideoEncode implements Runnable {
    private String MIME_TYPE = "video/avc";
    // 视频的比特率
    private final static int FORMAT_BITRATE = 125000;
    // 视频的帧率
    private final static int FORMAT_FPS = 30;

    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;

    private byte[] mHeadInfo = null;

    private String mSavePath;
    private FileOutputStream fileOutputStream;

    public VideoEncode(String mSavePath) {
        this.mSavePath = mSavePath;
    }

    public void init(int width, int height) {
        try {
            fileOutputStream = new FileOutputStream(mSavePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
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

    public void start() {
        mediaCodec.start();

    }

    public void readData(byte[] data) {
        int inputindex = mediaCodec.dequeueInputBuffer(-1);
        if (inputindex >= 0) {
            ByteBuffer inputBuffer = getInputBuffer(inputindex);
            inputBuffer.clear();
            inputBuffer.put(data);
            mediaCodec.queueInputBuffer(inputindex, 0, data.length, 0, 0);
        }

        MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
        int outindex = mediaCodec.dequeueOutputBuffer(mInfo, 1000);
        while (outindex >= 0) {
            ByteBuffer outBuffer = getOutputBuffer(outindex);
            byte[] outData = new byte[mInfo.size];
            outBuffer.get(outData);
            if (mInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                mHeadInfo = new byte[outData.length];
                mHeadInfo = outData;
            } else if (mInfo.flags % 8 == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                byte[] keyframe = new byte[outData.length + mHeadInfo.length];
                System.arraycopy(mHeadInfo, 0, keyframe, 0, mHeadInfo.length);
                System.arraycopy(outData, 0, keyframe, mHeadInfo.length, outData.length);
                try {
                    fileOutputStream.write(keyframe, 0, keyframe.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(mInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){

            }else {
                try {
                    fileOutputStream.write(outData, 0, outData.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mediaCodec.releaseOutputBuffer(outindex, false);
            outindex = mediaCodec.dequeueOutputBuffer(mInfo, 1000);
        }

    }

    public void stop(){
        mediaCodec.stop();
        mediaCodec.release();
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

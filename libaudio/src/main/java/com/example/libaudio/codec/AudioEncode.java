package com.example.libaudio.codec;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.example.libaudio.util.Constant;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/21
 * 编码将pcm 转 acc
 */
public class AudioEncode implements Runnable {
    private final static String TAG = "AudioEncode";
    private final static String MINE_TYPE = "audio/mp4a-latm";
    private int rate = 256000;
    private MediaCodec mediaCodec;
    private AudioRecord mAudioRecord;
    private FileOutputStream fos;
    private String mAudioPath;
    private int bufferSize;
    private byte[] buffer;
    private boolean isRecording;
    private Thread mThread;

    private int smapleRate = Constant.AUDIO_SAMPLERATEInHz;
    private int channelCount = 2;
    private int channelConfig = Constant.AUDIO_CHANNLE;
    private int audioFormat = Constant.AUDIO_FORMAT;

    public AudioEncode(String mAudioPath) {
        this.mAudioPath = mAudioPath;
    }

    public void prepare() {
        try {
            fos = new FileOutputStream(mAudioPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "file not found");
        }
        MediaFormat format = MediaFormat.createAudioFormat(MINE_TYPE, smapleRate, channelCount);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, rate);

        try {
            mediaCodec = MediaCodec.createEncoderByType(MINE_TYPE);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        bufferSize = AudioRecord.getMinBufferSize(smapleRate, channelConfig, audioFormat) * 2;
        buffer = new byte[bufferSize];
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, smapleRate, channelConfig, audioFormat, bufferSize);
    }

    public void start() throws InterruptedException {
        mediaCodec.start();
        mAudioRecord.startRecording();
        if (mThread != null && mThread.isAlive()) {
            isRecording = false;
            mThread.join();
        }
        isRecording = true;
        mThread = new Thread(this);
        mThread.start();
    }

    public void stop() {
        try {
            isRecording = false;
            mThread.join();
            mAudioRecord.stop();
            mediaCodec.stop();
            mediaCodec.release();
            fos.flush();
            fos.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }

    }

    public void readOutputData() {
        int index = mediaCodec.dequeueInputBuffer(-1);
        if (index >= 0) {
            ByteBuffer byteBuffer = getInputBuffer(index);
            byteBuffer.clear();
            int length = mAudioRecord.read(byteBuffer, bufferSize);
            if (length > 0) {
                mediaCodec.queueInputBuffer(index, 0, length, System.nanoTime() / 1000, 0);
            } else {
                Log.d(TAG, "length:" + length);
            }
        }

        MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
        int outIndex;

        outIndex = mediaCodec.dequeueOutputBuffer(mInfo, 10000);
        while (outIndex >= 0) {
            try {
                ByteBuffer outbyteBuffer = getOutputBuffer(index);
                outbyteBuffer.position(mInfo.offset);
                byte[] temp = new byte[mInfo.size + 7];
                outbyteBuffer.get(temp, 7, mInfo.size);
                addADTStoPacket(temp, temp.length);
                fos.write(temp);
                mediaCodec.releaseOutputBuffer(outIndex, false);
                outIndex = mediaCodec.dequeueOutputBuffer(mInfo, 10000);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "write fail");
            }

        }

    }

    /**
     * 给编码出的aac裸流添加adts头字段
     *
     * @param packet    要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
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
        readOutputData();
    }
}

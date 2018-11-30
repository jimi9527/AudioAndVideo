package com.example.libaudio.codec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import com.example.libaudio.util.Constant;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/23
 */
public class AudioDecode implements Runnable {

    private final static String TAG = "AudioDecode";
    private final static String MIME_TYPE = "audio/mpeg";
    private int smapleRate = Constant.AUDIO_SAMPLERATEInHz;
    private int channelCount = 2;

    private MediaCodec mediaCodec;
    private MediaExtractor mediaExtractor;

    private String mFilePath;
    private ArrayList<byte[]> chunkPCMDataContainer;//PCM数据块容器
    private Thread mThread;


    public AudioDecode(String mFilePath) {
        this.mFilePath = mFilePath;
        chunkPCMDataContainer= new ArrayList<>();
    }

    public void init() {
        try {
            mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(mFilePath);
            int trackCount = mediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    mediaExtractor.selectTrack(i);
                    Log.d(TAG, "mime:" + mime);

                    mediaCodec = MediaCodec.createDecoderByType(mime);
                    mediaCodec.configure(mediaFormat, null, null, 0);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaCodec == null) {
            Log.d(TAG, "create mediacodec fail");
            return;
        }

        mediaCodec.start();

    }

    public void start(){

    }

    public void decode() {
        int index = mediaCodec.dequeueInputBuffer(-1);
        if (index > 0) {

            ByteBuffer inputBuffer = getInputBuffer(index);
            inputBuffer.clear();
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
            if(sampleSize > 0){
                mediaCodec.queueInputBuffer(index, 0, sampleSize, 0, 0);
                mediaExtractor.advance();
            }else {
                Log.d(TAG, "sampleSize:" + sampleSize);
            }

        }
        MediaCodec.BufferInfo mInfo = new MediaCodec.BufferInfo();
        int outputIndex = mediaCodec.dequeueOutputBuffer(mInfo, 10000);

        while (outputIndex >= 0){
            // 拿到存放数据的buffer
            ByteBuffer outputBuffer = getOutputBuffer(outputIndex);
            byte[] temp = new byte[mInfo.size];
            // 将buffer数据渠道字节数组中
            outputBuffer.get(temp);
            outputBuffer.clear();
            putPCMData(temp);
            mediaCodec.releaseOutputBuffer(outputIndex, false);
            outputIndex = mediaCodec.dequeueOutputBuffer(mInfo, 10000);
        }
    }

    /**
     * 将PCM数据存入{@link #chunkPCMDataContainer}
     * @param pcmChunk PCM数据块
     */
    private void putPCMData(byte[] pcmChunk) {
        synchronized (AudioDecode.class) {//记得加锁
            chunkPCMDataContainer.add(pcmChunk);
        }
    }

    /**
     * 在Container中{@link #chunkPCMDataContainer}取出PCM数据
     * @return PCM数据块
     */
    private byte[] getPCMData() {
        synchronized (AudioDecode.class) {//记得加锁
            Log.d(TAG, "getPCM:"+chunkPCMDataContainer.size());
            if (chunkPCMDataContainer.isEmpty()) {
                return null;
            }

            byte[] pcmChunk = chunkPCMDataContainer.get(0);//每次取出index 0 的数据
            chunkPCMDataContainer.remove(pcmChunk);//取出后将此数据remove掉 既能保证PCM数据块的取出顺序 又能及时释放内存
            return pcmChunk;
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
        decode();
    }
}

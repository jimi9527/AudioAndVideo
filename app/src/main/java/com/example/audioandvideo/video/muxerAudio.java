package com.example.audioandvideo.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class MuxerAudio {
    private static final String TAG = "muxerAudio";
    private String sourchFile;
    private String outputFile;
    private MediaMuxer mediaMuxer;

    public MuxerAudio(String sourchFile, String outputFile) {
        this.sourchFile = sourchFile;
        this.outputFile = outputFile;
    }

    public void muxer(){
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(sourchFile);
            int audioTrackIndex = mediaExtractor.getTrackCount();
            for(int i = 0 ; i < audioTrackIndex; i++){
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mine = mediaFormat.getString(MediaFormat.KEY_MIME);
                if(mine.startsWith("audio/")){
                    audioTrackIndex = i;
                }
            }

            mediaExtractor.selectTrack(audioTrackIndex);
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(audioTrackIndex);
            mediaMuxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeAudioIndex = mediaMuxer.addTrack(trackFormat);
            mediaMuxer.start();

            Log.d(TAG, " start write audio");
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            long smapleTime = getSampleTime(mediaExtractor, byteBuffer);

            mediaExtractor.unselectTrack(audioTrackIndex);
            mediaExtractor.selectTrack(audioTrackIndex);

            while (true){
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if(readSampleSize < 0){
                    break;
                }
                mediaExtractor.advance();

                bufferInfo.presentationTimeUs += smapleTime;
                bufferInfo.offset = 0;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.size = readSampleSize;
                mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, bufferInfo);
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            mediaExtractor.release();
            Log.d(TAG, "end write audio");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //获取每帧之间的时间
    private long getSampleTime(MediaExtractor mediaExtractor, ByteBuffer byteBuffer){
        long videoSampleTime = 0;
        // 获取每帧之间的时间
        mediaExtractor.readSampleData(byteBuffer, 0);
        if(mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC){
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer, 0);
        long firstVideoPTS = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer, 0);
        long secondVideoPTS = mediaExtractor.getSampleTime();
        videoSampleTime = Math.abs(secondVideoPTS - firstVideoPTS);
        Log.d(TAG, "videoSampleTime is " + videoSampleTime);
        return videoSampleTime;
    }
}

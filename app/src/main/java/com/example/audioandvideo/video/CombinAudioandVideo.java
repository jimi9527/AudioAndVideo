package com.example.audioandvideo.video;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.winom.olog.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class CombinAudioandVideo {
    private static final String TAG = "CombinAudioandVideo";
    private String outputFile;
    private String outputAudioFile;
    private String outCombinFile;

    public CombinAudioandVideo(String outputFile, String outputAudioFile, String outCombinFile) {
        this.outputFile = outputFile;
        this.outputAudioFile = outputAudioFile;
        this.outCombinFile = outCombinFile;
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

    // 合成视频
    public void combinVideo(){
        MediaExtractor videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(outputFile);
            int videoTrackIndex = videoExtractor.getTrackCount();
            MediaFormat videoFormat = null;
            for(int i = 0; i < videoTrackIndex; i++){
                videoFormat = videoExtractor.getTrackFormat(i);
                String mine = videoFormat.getString(MediaFormat.KEY_MIME);
                if(mine.startsWith("video/")){
                    videoTrackIndex = i;
                    break;
                }
            }

            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(outputAudioFile);
            int audioTrackIndex = audioExtractor.getTrackCount();
            MediaFormat audioFormat = null;
            for(int i = 0; i < audioTrackIndex; i++){
                audioFormat = audioExtractor.getTrackFormat(i);
                String mine = audioFormat.getString(MediaFormat.KEY_MIME);
                if(mine.startsWith("audio/")){
                    audioTrackIndex = i;
                    break;
                }
            }

            videoExtractor.selectTrack(videoTrackIndex);
            audioExtractor.selectTrack(audioTrackIndex);

            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            MediaMuxer mediaMuxer = new MediaMuxer(outCombinFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int writeVideoIndex = mediaMuxer.addTrack(videoFormat);
            int writeAudioIndex = mediaMuxer.addTrack(audioFormat);
            mediaMuxer.start();
            Log.d(TAG, "combin start");

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            long sampleTime = getSampleTime(videoExtractor, byteBuffer);
            videoExtractor.unselectTrack(videoTrackIndex);
            videoExtractor.selectTrack(videoTrackIndex);

            while (true){
                int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
                if(readVideoSampleSize < 0){
                    break;
                }
                videoBufferInfo.size = readVideoSampleSize;
                videoBufferInfo.presentationTimeUs += sampleTime;
                videoBufferInfo.offset = 0;
                videoBufferInfo.flags = videoExtractor.getSampleFlags();

                mediaMuxer.writeSampleData(writeVideoIndex, byteBuffer, videoBufferInfo);
                videoExtractor.advance();
            }

            while (true){
                int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
                if(readAudioSampleSize < 0){
                    break;
                }
                audioBufferInfo.size = readAudioSampleSize;
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                audioBufferInfo.offset = 0;
                audioBufferInfo.presentationTimeUs += sampleTime;
                mediaMuxer.writeSampleData(writeAudioIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            mediaMuxer.stop();
            mediaMuxer.release();
            videoExtractor.release();
            audioExtractor.release();
            Log.d(TAG, "combin end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

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
public class MuxerVideo {
    private static final String TAG = "muxerVideo";
    private String sourchFile;
    private String outputFile;
    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;

    public MuxerVideo(String sourchFile, String outputFile) {
        this.sourchFile = sourchFile;
        this.outputFile = outputFile;
    }
    // 分离视频
    public void muxer(){
        mediaExtractor = new MediaExtractor();
        int videoIndex = -1;
        try {
            mediaExtractor.setDataSource(sourchFile);
            int trackCount = mediaExtractor.getTrackCount();
            for(int i = 0; i < trackCount; i++){
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if(mimeType.startsWith("video/")){
                    videoIndex = i;
                }
            }

            mediaExtractor.selectTrack(videoIndex);
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(videoIndex);
            mediaMuxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            int trackIndex = mediaMuxer.addTrack(mediaFormat);
            ByteBuffer byteBuffer = ByteBuffer.allocate(1204 * 500);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mediaMuxer.start();
            Log.d(TAG, "start write video");
            long videoSampleTime = getSampleTime(mediaExtractor, byteBuffer);

            mediaExtractor.unselectTrack(videoIndex);
            mediaExtractor.selectTrack(videoIndex);

            while (true){
                int readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0);
                if(readSampleSize < 0){
                    break;
                }
                mediaExtractor.advance();
                bufferInfo.size = readSampleSize;
                bufferInfo.offset = 0;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.presentationTimeUs += videoSampleTime;
                mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            }

            mediaMuxer.stop();
            mediaExtractor.release();
            mediaMuxer.release();
            Log.d(TAG, "write video");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取每帧之间的时间
    private long getSampleTime(MediaExtractor mediaExtractor, ByteBuffer byteBuffer) {
        long videoSampleTime = 0;
        // 获取每帧之间的时间
        mediaExtractor.readSampleData(byteBuffer, 0);
        if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
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

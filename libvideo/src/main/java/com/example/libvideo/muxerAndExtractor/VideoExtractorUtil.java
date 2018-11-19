package com.example.libvideo.muxerAndExtractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/16
 */
public class VideoExtractorUtil {
    private final static String FORMAT_VIDEO = "video/";
    private final static String FORMAT_AUDIO = "audio/";
    private final static String TAG = "VideoExtractorUtil";
    private File videoPath;
    private File output_videoPath;
    private File output_audioPath;

    public VideoExtractorUtil(File videoPath, File output_videoPath, File output_audioPath) {
        this.videoPath = videoPath;
        this.output_videoPath = output_videoPath;
        this.output_audioPath = output_audioPath;
    }

    public void extractor() {
        try {

            FileOutputStream videoOutputStream = new FileOutputStream(output_videoPath);
            FileOutputStream audioOutputStream = new FileOutputStream(output_audioPath);

            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(videoPath.getAbsolutePath());
            int numTracks = extractor.getTrackCount();
            int audioTrackIndex = -1;
            int videoTrackIndex = -1;

            for(int i = 0; i < numTracks; i++){
                MediaFormat format = extractor.getTrackFormat(i);
                String mine = format.getString(MediaFormat.KEY_MIME);
                if(mine.startsWith(FORMAT_VIDEO)){
                    videoTrackIndex = i;
                }

                if(mine.startsWith(FORMAT_AUDIO)){
                    audioTrackIndex = i;
                }
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            // 切换到视频信息通道
            extractor.selectTrack(videoTrackIndex);
            Log.d(TAG, " start write video data ");
            while (true){
                int readSampleCount = extractor.readSampleData(byteBuffer, 0);
                if(readSampleCount < 0){
                    break;
                }
             byte[] buffer = new byte[readSampleCount];
             byteBuffer.get(buffer);
             videoOutputStream.write(buffer);
             byteBuffer.clear();
             extractor.advance();
            }
            Log.d(TAG, " start write audio data ");
            // 切换到音频信息通道
            extractor.selectTrack(audioTrackIndex);
            while (true){
                int readSampleCount = extractor.readSampleData(byteBuffer, 0);
                if(readSampleCount < 0){
                    break;
                }
                byte[] buffer = new byte[readSampleCount];
                byteBuffer.get(buffer);
                audioOutputStream.write(buffer);
                byteBuffer.clear();
                extractor.advance();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

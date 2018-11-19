package com.example.audioandvideo;

import android.os.Bundle;
import android.print.PrinterId;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.audioandvideo.video.CombinAudioandVideo;
import com.example.audioandvideo.video.MuxerAudio;
import com.example.audioandvideo.video.MuxerVideo;
import com.example.libaudio.util.Constant;
import java.io.File;
import java.io.IOException;


/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class MuxerActivity extends BaseActivity{
    private final static String TAG = "MuxerActivity";
    private String videoPath;
    private String outputVideoPath;
    private String outputAudioPath;
    private String combinPath;
    @Override
    public int getLayout() {
        return R.layout.activity_muxer;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFile();
    }

    private void initFile() {
        File mVideoFilePath = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.VIDEO_FILE_NAME);
        if(mVideoFilePath.exists()){
            videoPath = mVideoFilePath.getAbsolutePath();
            Log.d(TAG, "videoPath:" + videoPath);
        }

        File outputVideoFile = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.OUTPUT_VIDEO);
        if(!outputVideoFile.exists()){
            try {
                outputVideoFile.createNewFile();
                Log.d(TAG, "create outputvideo file success");
                outputVideoPath = outputVideoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "create  outputaudiofile fail");
            }
        }else {
            outputVideoPath = outputVideoFile.getAbsolutePath();
        }

        File outputAudioFile = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.OUTPUT_AUDIO);
        if(!outputAudioFile.exists()){
            try {
                outputAudioFile.createNewFile();
                Log.d(TAG, "create outputaudio file success");
                outputAudioPath = outputAudioFile.getAbsolutePath();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "create  outputaudiofile fail");
            }
        }else {
            outputAudioPath = outputAudioFile.getAbsolutePath();
        }

        File combinFile = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.COMBIN_FILE);
        if(!combinFile.exists()){
            try {
                combinFile.createNewFile();
                combinPath = combinFile.getAbsolutePath();
                Log.d(TAG, "create combinfile success");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "create  combinfile fail");
            }
        }else {
            combinPath = combinFile.getAbsolutePath();
        }

    }

    public void muxer_audio(View view){
        MuxerAudio muxerAudio = new MuxerAudio(videoPath, outputAudioPath);
        muxerAudio.muxer();
    }

    public void muxer_video(View view){
        MuxerVideo muxerVideo = new MuxerVideo(videoPath, outputVideoPath);
        muxerVideo.muxer();
    }

    public void muxer_video_audio(View view){
            new CombinThread().start();
    }

    class CombinThread extends Thread{
        @Override
        public void run() {
            super.run();
            CombinAudioandVideo combinAudioandVideo = new CombinAudioandVideo(outputVideoPath, outputAudioPath, combinPath);
            combinAudioandVideo.combinVideo();
        }
    }
}

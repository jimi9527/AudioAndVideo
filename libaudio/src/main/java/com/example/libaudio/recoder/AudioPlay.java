package com.example.libaudio.recoder;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import com.example.libaudio.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/10
 */
public class AudioPlay {
    private final static String TAG = "AudioPlay";
    private AudioTrack mAudioTrack;
    private int mMinBufferSize;
    private boolean isplay;

    public void initAudioTrack(){

        mMinBufferSize = AudioTrack.getMinBufferSize(Constant.AUDIO_SAMPLERATEInHz, Constant.AUDIO_CHANNLE, Constant.AUDIO_FORMAT);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(Constant.AUDIO_SAMPLERATEInHz)
                 .setEncoding(AudioFormat.ENCODING_PCM_16BIT)

                .build();

        mAudioTrack = new AudioTrack(audioAttributes, audioFormat, mMinBufferSize, AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE) ;

    }

    // 播放
    public void playAudio(){
        if(mAudioTrack != null){
            isplay = true;
            mAudioTrack.play();
            new PlayThread().start();
        }
    }

    // 停止
    public void stopAudio(){
        if(mAudioTrack != null){
            mAudioTrack.stop();
            isplay = false;
        }
    }

    private class PlayThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                byte[] data = new byte[mMinBufferSize];
                File audioFile = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.AUDIO_FILE_NAME);
                if(!audioFile.exists()){
                    Log.d(TAG, "audio is not exists");
                    return;
                }
                FileInputStream fileInputStream = new FileInputStream(audioFile);
                while (fileInputStream.read(data) >= 0 && isplay){
                    mAudioTrack.write(data, 0, data.length);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "audio file not found");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



}

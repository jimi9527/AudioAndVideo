package com.example.libaudio.recoder;
import android.media.AudioRecord;
import android.util.Log;

import com.example.libaudio.util.Constant;
import com.example.libaudio.util.WavWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/5
 */
public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    public  int AUDIO_BUFFSIZE = 0;

    private AudioRecord mAudioRecord;
    private boolean isRecord;
    private File mAudioFile;
    private WavWriter mWavWriter;
    private String mWavFilePath;
    private int mCurAmplitude;

    public AudioRecorder(){

    }

    // 初始化AudioRecord
    public void initAudioRecord(){
        AUDIO_BUFFSIZE = AudioRecord.getMinBufferSize(Constant.AUDIO_SAMPLERATEInHz, Constant.AUDIO_CHANNLE,  Constant.AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(Constant.AUDIO_SOURCE, Constant.AUDIO_SAMPLERATEInHz, Constant.AUDIO_CHANNLE,
               Constant.AUDIO_FORMAT, AUDIO_BUFFSIZE);


        mAudioFile = new File(Constant.CONTANT_PATH + Constant.AUDIO + Constant.AUDIO_FILE_NAME);
            if(!mAudioFile.exists()){
                try {
                    mAudioFile.createNewFile();
                    Log.e(TAG, "mAudioFile created success");
                    Log.e(TAG, "mAudioFile.path:" + mAudioFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "mAudioFile created IOException");
                }

            }

        mWavFilePath = Constant.CONTANT_PATH + Constant.AUDIO + Constant.ADUIO_WAV_FILE_NAME;
        mWavWriter = new WavWriter(mWavFilePath, Constant.AUDIO_CHANNLE, Constant.AUDIO_SAMPLERATEInHz, Constant.AUDIO_FORMAT);
    }

    // 开始录制
    public void startRecord(){
        mAudioRecord.startRecording();
        isRecord = true;
        new AudioRecorderThread().start();
    }
    // 停止录制
    public void stopRecord(){
        isRecord = false;
        mAudioRecord.stop();
    }


    // 录制线程
    private class AudioRecorderThread extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                final byte data[] = new byte[AUDIO_BUFFSIZE];
                FileOutputStream os = new FileOutputStream(mAudioFile);
                if(os != null){
                    while (isRecord){
                        int read = mAudioRecord.read(data, 0, AUDIO_BUFFSIZE);
                        if(AudioRecord.ERROR_INVALID_OPERATION != read){
                                os.write(data);
                                mWavWriter.writeToFile(data, read);
                                setCurAmplitude(data, read);
                        }
                    }
                }
                Log.d(TAG, "write end");
                os.close();
                mWavWriter.closeFile();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "file not found");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "wrirte fail");
            }

        }
    }

    private void setCurAmplitude(byte[] readBuf, int read) {
        mCurAmplitude = 0;
        for (int i = 0; i < read / 2; i++) {
            short curSample = (short) ((readBuf[i * 2] & 0xFF) | (readBuf[i * 2 + 1] << 8));
            if (curSample > mCurAmplitude) {
                mCurAmplitude = curSample;
            }
        }
    }

}

package com.example.audioandvideo;

import android.Manifest;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.libaudio.recoder.AudioPlay;
import com.example.libaudio.recoder.AudioRecorder;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;


import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private AudioRecorder audioRecorder;
    private AudioPlay audioPlay;
    private Button mStartRecorder, mStopRecorder, mStartPlay, mStopPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();

    }
    private void initPermission(){
        RxPermissions rxPermissions = new RxPermissions(this);

            rxPermissions.requestEach(Manifest.permission.RECORD_AUDIO )
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if(permission.granted){
                                // 用户已经同意该权限
                                Log.d(TAG, permission.name + " is granted.");
                                initAudio();
                            }else if(permission.shouldShowRequestPermissionRationale){
                                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                Log.d(TAG, permission.name + " is denied. More info should be provided.");
                            }else {
                                // 用户拒绝了该权限，并且选中『不再询问』
                                Log.d(TAG, permission.name + " is denied.");
                            }
                        }
                    });

    }

    private void initAudio() {
        mStartRecorder = findViewById(R.id.start_record);
        mStopRecorder = findViewById(R.id.stop_record);
        mStartPlay = findViewById(R.id.play_audio);
        mStopPlay = findViewById(R.id.stop_audio);

        mStartRecorder.setOnClickListener(this);
        mStopRecorder.setOnClickListener(this);
        mStartPlay.setOnClickListener(this);
        mStopPlay.setOnClickListener(this);

        audioRecorder = new AudioRecorder();
        audioRecorder.initAudioRecord();

        audioPlay = new AudioPlay();
        audioPlay.initAudioTrack();
      //  AudioPlay.getInstance().initAudioTrack();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_record:
                audioRecorder.startRecord();
                break;
            case R.id.stop_record:
                audioRecorder.stopRecord();
                break;
            case R.id.play_audio:
                audioPlay.playAudio();
                break;
            case R.id.stop_audio:
                audioPlay.stopAudio();
                break;
        }
    }
}

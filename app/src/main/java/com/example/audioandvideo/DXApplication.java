package com.example.audioandvideo;

import android.app.Application;

import com.winom.olog.Log;
import com.winom.olog.LogImpl;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/13
 */
public class DXApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Log.setLogImpl(new LogImpl(Constant.LOG_FILE_PATH, Constant.LOG_FILE_NAME, ".olog"));
        Log.setLogToLogcat(true);

    }
}

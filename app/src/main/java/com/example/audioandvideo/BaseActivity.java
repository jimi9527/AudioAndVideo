package com.example.audioandvideo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/15
 */
public abstract class BaseActivity extends AppCompatActivity {


    public abstract int getLayout();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
    }

    private void checkPer(String[] strings) {
       // RxPermissions rxPermissions = new RxPermissions();

    }
}

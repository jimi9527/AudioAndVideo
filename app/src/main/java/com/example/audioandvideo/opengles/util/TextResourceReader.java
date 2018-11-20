package com.example.audioandvideo.opengles.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * author: tonydeng
 * mail : tonydeng@hxy.com
 * 2018/11/19
 */
public class TextResourceReader {


    public static String readTextFileFromResource(Context context, int resourceId){
        StringBuilder body = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextline;
            while ((nextline = bufferedReader.readLine()) != null){
                body.append(nextline);
                body.append("/n");
            }
        }catch (IOException e){
               throw new RuntimeException("could not open resource:" + resourceId, e);
        }
        return body.toString();
    }
}

package com.nfcread.com.sampleandroid;

import android.app.Application;
import android.content.Context;


public class App extends Application {

    private static App instance;


    public static App getInstance ()
    {
        return instance;
    }


}

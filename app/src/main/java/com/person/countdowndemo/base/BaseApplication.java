package com.person.countdowndemo.base;

import android.app.Application;

import com.person.countdowndemo.utils.Utils;

/**
 *
 **/
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);

    }
}

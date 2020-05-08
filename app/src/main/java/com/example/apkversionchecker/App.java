package com.example.apkversionchecker;

import android.app.Application;

import com.dhy.xintent.ActivityKiller;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActivityKiller.init(this);
    }
}

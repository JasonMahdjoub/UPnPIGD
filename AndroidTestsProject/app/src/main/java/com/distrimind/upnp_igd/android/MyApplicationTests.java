package com.distrimind.upnp_igd.android;

import android.app.Application;

import com.distrimind.flexilogxml.android.ContextProvider;

public class MyApplicationTests extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ContextProvider.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ContextProvider.applicationClosed();
    }
}

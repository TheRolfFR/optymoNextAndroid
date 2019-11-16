package com.therolf.optymoNext.controller;

import android.app.Application;
import android.util.Log;

public class GlobalApplication extends Application {

    private NetworkController networkController;

    @Override
    public void onCreate() {
        networkController = new NetworkController();
        networkController.generate(this);

        Log.d("optymonext", "creating application instance");
        super.onCreate();
    }

    public NetworkController getNetworkController() {
        return networkController;
    }
}

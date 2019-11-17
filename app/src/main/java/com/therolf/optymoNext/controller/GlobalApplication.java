package com.therolf.optymoNext.controller;

import android.app.Application;
import android.util.Log;

public class GlobalApplication extends Application {

    private FavoritesController favoritesController;
    private NetworkController networkController;

    @Override
    public void onCreate() {
        networkController = new NetworkController();
        networkController.generate(this);

        favoritesController = new FavoritesController();
        favoritesController.readFile(this);

        Log.d("optymonext", "creating application instance");
        super.onCreate();
    }

    public NetworkController getNetworkController() {
        return networkController;
    }

    public FavoritesController getFavoritesController() {
        return favoritesController;
    }
}

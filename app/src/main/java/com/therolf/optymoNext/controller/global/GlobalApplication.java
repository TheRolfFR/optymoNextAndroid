package com.therolf.optymoNext.controller.global;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

public class GlobalApplication extends Application {

    private FavoritesController favoritesController;
    private NetworkController networkController;

    @Override
    public void onCreate() {
        networkController = new NetworkController();
        new GenerateNetworkRequest().execute(this);

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

    private static class GenerateNetworkRequest extends AsyncTask<GlobalApplication, Void, Void> {

        @Override
        protected Void doInBackground(GlobalApplication... contexts) {
            if(contexts.length  > 0) {
                contexts[0].networkController.generate(contexts[0]);
            }

            return null;
        }
    }
}

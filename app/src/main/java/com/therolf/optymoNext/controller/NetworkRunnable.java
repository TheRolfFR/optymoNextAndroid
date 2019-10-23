package com.therolf.optymoNext.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.therolf.optymoNextModel.OptymoNetwork;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NetworkRunnable implements Runnable {

    @SuppressLint("StaticFieldLeak")
    private static NetworkRunnable instance = new NetworkRunnable();
    private Context context;
    private boolean generatedOnce = false;

    public static NetworkRunnable getInstance(OptymoNetwork.ProgressListener listener, Context context) {
        if(instance.generatedOnce)
            listener.OnGenerationEnd(true);
        OptymoNetworkController.getInstance().setProgressListener(listener);
        return getInstance(context);
    }

    public static NetworkRunnable getInstance(Context context) {
        instance.context = context;
        System.out.println(context);
        return instance;
    }

    @Override
    public void run() {
        if(!generatedOnce) {
            GenerationRequest gRequest = new GenerationRequest();
            gRequest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            generatedOnce = true;
        }
    }

    private class GenerationRequest extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            OptymoNetworkController.getInstance().generate(context);
            return null;
        }
    }
}

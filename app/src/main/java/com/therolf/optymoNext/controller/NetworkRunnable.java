package com.therolf.optymoNext.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.therolf.optymoNextModel.OptymoNetwork;

import java.lang.ref.WeakReference;

public class NetworkRunnable implements Runnable {

    @SuppressLint("StaticFieldLeak")
    private static NetworkRunnable instance = new NetworkRunnable();
    private Context context;
    private boolean generatedOnce = false;

    public static NetworkRunnable getInstance(OptymoNetwork.ProgressListener listener, Context context) {
        if(instance.generatedOnce)
            listener.OnGenerationEnd(true);
        NetworkController.getInstance().setProgressListener(listener);
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
            GenerationRequest gRequest = new GenerationRequest(this);
            gRequest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            generatedOnce = true;
        }
    }

    private static class GenerationRequest extends AsyncTask<Void, Void, Void> {

        private WeakReference<NetworkRunnable> activityReference;

        GenerationRequest(NetworkRunnable runnable) {
            this.activityReference = new WeakReference<>(runnable);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // get runnable
            NetworkRunnable runnable = activityReference.get();
            if (runnable == null) return null;

            NetworkController.getInstance().generate(runnable.context);
            return null;
        }
    }
}

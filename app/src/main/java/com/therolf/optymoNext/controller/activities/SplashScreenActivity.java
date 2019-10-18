package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNextModel.OptymoNetwork;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "splash";

    private OptymoNetworkController networkController;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        final ProgressBar genProgressBar = findViewById(R.id.splash_generation_progress_bar);
        final ProgressBar roundProgressBar = findViewById(R.id.splash_progress_2);

        networkController = OptymoNetworkController.getInstance();
        networkController.setProgressListener(new OptymoNetwork.ProgressListener() {
            @Override
            public void OnProgressUpdate(float progress) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    genProgressBar.setProgress(Math.round(progress*100), true);
                    roundProgressBar.setProgress(Math.round(progress*100), true);
                } else {
                    genProgressBar.setProgress(Math.round(progress*100));
                    roundProgressBar.setProgress(Math.round(progress*100));
                }
            }

            @Override
            public void OnGenerationEnd(boolean returnValue) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    genProgressBar.setProgress(100, true);
                    roundProgressBar.setProgress(100, true);
                } else {
                    genProgressBar.setProgress(100);
                    roundProgressBar.setProgress(100);
                }
                if(returnValue)
                    goToMain();
            }
        });

        new OptymoNetworkGen().execute(SplashScreenActivity.this);
    }

    void goToMain() {
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    class OptymoNetworkGen extends AsyncTask<Context, Void, Void> {
        @Override
        protected Void doInBackground(Context... contexts) {
            networkController.generate(contexts[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}

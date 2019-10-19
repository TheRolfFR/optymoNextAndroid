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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNextModel.OptymoNetwork;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "splash";

    private OptymoNetworkController networkController;
    private boolean generatedNetwork = false;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        final ProgressBar roundProgressBar = findViewById(R.id.splash_progress);
        final TextView loadingText = findViewById(R.id.splash_loading_text);

        networkController = OptymoNetworkController.getInstance();
        networkController.setProgressListener(new OptymoNetwork.ProgressListener() {
            @Override
            public void OnProgressUpdate(int current, int total, String message) {
                switch (message) {
                    case "XML":
                        loadingText.setText(R.string.splash_xml_generation_text);
                        break;
                    case "JSON":
                        loadingText.setText(R.string.splash_json_loading_text);
                        break;
                    case "line":
                        loadingText.setText(getResources().getString(R.string.splash_line, current, total));
                        break;
                    case "stop":
                        loadingText.setText(getResources().getString(R.string.splash_stop, current, total));
                        break;
                    default:
                        System.err.println(message);
                        loadingText.setText(R.string.splash_error_loading_text);
                        break;
                }
            }

            @Override
            public void OnGenerationEnd(boolean returnValue) {
                generatedNetwork = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    roundProgressBar.setProgress(100, true);
                } else {
                    roundProgressBar.setProgress(100);
                }
                if(returnValue)
                    goToMain();
            }
        });

        if(generatedNetwork) {
            goToMain();
        } else {
            new OptymoNetworkGen().execute(SplashScreenActivity.this);
        }
    }

    void goToMain() {
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_CANCELED) {
            goToMain();
        }
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

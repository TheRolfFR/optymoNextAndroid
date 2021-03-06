package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.global.GlobalApplication;
import com.therolf.optymoNext.controller.activities.Main.MainActivity;
import com.therolf.optymoNextModel.OptymoNetwork;

public class SplashScreenActivity extends AppCompatActivity implements OptymoNetwork.ProgressListener {

    @SuppressWarnings("unused")
    private static final String TAG = "splash";

    private TextView loadingText;
    private TextView errorText;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        loadingText = findViewById(R.id.splash_loading_text);
        errorText = findViewById(R.id.splash_error_text);

        ((GlobalApplication) getApplication()).getFavoritesController().addProgressListenerIfNotLoaded(this);
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

    public void OnProgressUpdate(final int current, final int total, final String message) {
        runOnUiThread(() -> updateUI(current, total, message));
    }

    void updateUI(int current, int total, String message) {
        switch (message) {
            case "gen_stop":
                loadingText.setText(getResources().getString(R.string.splash_generating_stop, current, total));
            case "XML":
                loadingText.setText(R.string.splash_generating_xml);
                break;
            case "JSON":
                loadingText.setText(R.string.splash_json_loading_text);
                break;
            case "line":
                loadingText.setText(getResources().getString(R.string.splash_loading_line, current, total));
                break;
            case "stop":
                loadingText.setText(getResources().getString(R.string.splash_loading_stop, current, total));
                break;
            case "favorite":
                loadingText.setText(getResources().getString(R.string.splash_favorite_loading, current, total));
                break;
            default:
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(SplashScreenActivity.this.getResources().getString(R.string.splash_error_text, message));
                loadingText.setText(R.string.splash_error_loading_text);
                break;
        }
    }

    @Override
    public void OnGenerationEnd(boolean returnValue) {
        if(returnValue)
            goToMain();
    }
}

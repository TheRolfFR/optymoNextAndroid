package com.therolf.optymoNext.controller.activities.Main;

import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.global.GlobalApplication;
import com.therolf.optymoNext.controller.activities.TopViewActivity;
import com.therolf.optymoNextModel.OptymoNetwork;

class SnackBarController {

    static void run(TopViewActivity activity, ImageView searchButton, FloatingActionButton fab, ImageButton addButton) {
        // create the snackbar
        Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.coordinator_layout), activity.getResources().getString(R.string.splash_loading_network), Snackbar.LENGTH_INDEFINITE);

        // change the color of the snackbar
        snackbar.getView().setBackgroundColor(activity.getResources().getColor(R.color.colorPrimaryDark));

        // modify bottom margin
        snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
            }

            @Override
            public void onShown(Snackbar transientBottomBar) {
                super.onShown(transientBottomBar);
            }
        });

        // show snackbar
        snackbar.show();

        // change snackbar text
        ((GlobalApplication) activity.getApplication()).getNetworkController().addProgressListenerIfNotGenenerated(new OptymoNetwork.ProgressListener() {
            @Override
            public void OnProgressUpdate(int current, int total, String message) {
                activity.runOnUiThread(() -> {
                    switch (message) {
                        case "gen_stop":
                            snackbar.setText(activity.getResources().getString(R.string.splash_generating_stop, current, total));
                        case "XML":
                            snackbar.setText(R.string.splash_generating_xml);
                            break;
                        case "JSON":
                            snackbar.setText(R.string.splash_json_loading_text);
                            break;
                        case "line":
                            snackbar.setText(activity.getResources().getString(R.string.splash_loading_line, current, total));
                            break;
                        case "stop":
                            snackbar.setText(activity.getResources().getString(R.string.splash_loading_stop, current, total));
                            break;
                        case "favorite":
                            snackbar.setText(activity.getResources().getString(R.string.splash_favorite_loading, current, total));
                            break;
                        default:
                            snackbar.setText(R.string.splash_error_loading_text);
                            break;
                    }
                });
            }

            @Override
            public void OnGenerationEnd(boolean returnValue) {
                activity.runOnUiThread(() -> {
                    snackbar.dismiss();
                    searchButton.setEnabled(true);
                    fab.setEnabled(true);
                    addButton.setEnabled(true);
                });
            }
        });

    }
}

package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.FavoritesController;
import com.therolf.optymoNext.controller.GlobalApplication;
import com.therolf.optymoNext.controller.NetworkRunnable;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.vue.adapters.OptymoNextTimeAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


@SuppressLint("StaticFieldLeak")
@SuppressWarnings("unused")
public class MainActivity extends TopViewActivity {

    private ListView favoriteList;
    private SwipeRefreshLayout refreshLayout;
    private TextView lastUpdateText;

    // favorites
    int numberOfUpdated = 0;
    private ArrayList<OptymoGetNextTime> nextTimeRequests = new ArrayList<>();
    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();
    private OptymoNextTimeAdapter favoritesAdapter;
    private DateFormat dateFormat;

    // search part
    private ImageView searchButton;
    private Dialog searchDialog;
    @SuppressWarnings("FieldCanBeLocal")
    private com.ferfalk.simplesearchview.SimpleSearchView searchView;

    private static Intent addFavoriteActivity = null;

    public static void destroyIntent() {
        addFavoriteActivity = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.updateListHeight();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main, true);
        OptymoNetworkController networkController = OptymoNetworkController.getInstance();
        Log.e("lines number", "" + networkController.getLines().length);

        // favorite list (BEFORE refreshFavoriteList)
        favoriteList = findViewById(R.id.main_favorite_next_stops);
        favoritesAdapter = new OptymoNextTimeAdapter(this, nextTimes);
        favoriteList.setAdapter(favoritesAdapter);

        // refresh layout
        refreshLayout = findViewById(R.id.main_refresh_layout);
        refreshLayout.setColorSchemeResources(
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement);

        // last updated text (BEFORE refreshFavoriteList)
        lastUpdateText = findViewById(R.id.main_last_update_text);

        // date format
        dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);

        refreshFavoriteList();
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            refreshFavoriteList();
        });

        // favoriteList on long press delete element
        favoriteList.setOnItemLongClickListener((adapterView, view, position, id) -> {
            final int pos = position;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme_CustomDialog);
            builder.setCancelable(true);
            builder.setTitle(R.string.dialog_del_fav_title);
            builder.setMessage(getResources().getString(R.string.dialog_del_fav_message, nextTimes.get(pos).directionToString()));
            builder.setPositiveButton(R.string.yes,
                    (dialog, which) -> {
                        FavoritesController.getInstance().remove(nextTimes.get(pos), MainActivity.this);
                        refreshFavoriteList();
                    });
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        });

        // search icon
        findViewById(R.id.top_search_icon).setVisibility(View.VISIBLE);

        // bottom floating button
        com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(view -> {
            if(addFavoriteActivity == null) {
                addFavoriteActivity = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(addFavoriteActivity);
            }
        });

        // search part
        searchDialog = new Dialog(this);
        searchDialog.setContentView(R.layout.dialog_search);
        Window window = searchDialog.getWindow();
        if(window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        searchView = searchDialog.findViewById(R.id.searchView);
        System.out.println("" + searchButton + "" + searchDialog + searchView);
        searchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SimpleSearchView", "Submit:" + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SimpleSearchView", "Text changed:" + newText);
                return false;
            }

            @Override
            public boolean onQueryTextCleared() {
                Log.d("SimpleSearchView", "Text cleared");
                return false;
            }
        });
        searchButton = findViewById(R.id.top_search_icon);
        searchButton.setEnabled(false);
        searchButton.setOnClickListener(view -> searchDialog.show());

        // snackbar
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), getResources().getString(R.string.splash_loading_network), Snackbar.LENGTH_INDEFINITE);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
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
        snackbar.show();
        NetworkRunnable.getInstance(new OptymoNetwork.ProgressListener() {
            @Override
            public void OnProgressUpdate(int current, int total, String message) {
                MainActivity.this.runOnUiThread(() -> {
                    switch (message) {
                        case "gen_stop":
                            snackbar.setText(getResources().getString(R.string.splash_generating_stop, current, total));
                        case "XML":
                            snackbar.setText(R.string.splash_generating_xml);
                            break;
                        case "JSON":
                            snackbar.setText(R.string.splash_json_loading_text);
                            break;
                        case "line":
                            snackbar.setText(getResources().getString(R.string.splash_loading_line, current, total));
                            break;
                        case "stop":
                            snackbar.setText(getResources().getString(R.string.splash_loading_stop, current, total));
                            break;
                        case "favorite":
                            snackbar.setText(getResources().getString(R.string.splash_favorite_loading, current, total));
                            break;
                        default:
                            snackbar.setText(R.string.splash_error_loading_text);
                            break;
                    }
                });
            }

            @Override
            public void OnGenerationEnd(boolean returnValue) {
                MainActivity.this.runOnUiThread(() -> {
                    snackbar.dismiss();
                    searchButton.setEnabled(true);
                    fab.setEnabled(true);
                });
            }
        }, GlobalApplication.getContext()).run();
    }

    private void refreshFavoriteList() {
        Toast.makeText(this, R.string.main_toast_loading_favorites, Toast.LENGTH_SHORT).show();

        OptymoDirection[] directions = FavoritesController.getInstance().getFavorites();
//        System.out.println(Arrays.toString(directions));

        // if you have no favorites
        // BUG FIX no favorite infinite refreshing
        if(directions.length == 0) {
            refreshLayout.setRefreshing(false);

            // update data
            favoritesAdapter.notifyDataSetChanged();
            // update height
            this.updateListHeight();
        }

        // we reset number of updated
        numberOfUpdated = 0;
        nextTimes.clear();

        // we add all the new favorites
        // TODO Add the ones you have and remove the old ones
        // 1. Stop the remaining requests
        OptymoGetNextTime tmp;
        while (nextTimeRequests.size() > 0){
            tmp = nextTimeRequests.get(0);
            if(tmp != null) {
                tmp.cancel(true);
            }
            nextTimeRequests.remove(0);
        }

        // 2. we add and make a request for all favorites
        for(OptymoDirection direction : directions) {
            // create a new request
            tmp = new OptymoGetNextTime();
            // save the new request
            nextTimeRequests.add(tmp);
            // execute the new request
            tmp.execute(direction);
        }
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent();
        i.putExtra("HELLO_WORLD", "!");
        setResult(2, i);
        super.onDestroy();
    }

    private void updateListHeight() {
        Utility.setListViewHeightBasedOnChildren(favoriteList);
    }

    class OptymoGetNextTime extends AsyncTask<OptymoDirection, Void, OptymoNextTime> {
        @Override
        protected OptymoNextTime doInBackground(OptymoDirection... optymoDirections) {
            OptymoNextTime result = null;
            OptymoNextTime latestResult = null;

            OptymoNextTime[]nextTimes = OptymoNetwork.getNextTimes(optymoDirections[0].getStopSlug());
            int i = 0;
            while(i < nextTimes.length && result == null) {
                if(optymoDirections[0].toString().equals(nextTimes[i].directionToString()))
                    result = nextTimes[i];
                if(latestResult == null) {
                    latestResult = nextTimes[i];
                } else if(latestResult.compareTo(nextTimes[i]) < 0)
                    latestResult = nextTimes[i];
                ++i;
            }

            // provide default result > last time got
            if(result == null) {
                String nullResult = OptymoNextTime.NULL_TIME_VALUE;
                if(latestResult != null)
                    nullResult = "> " + latestResult.getNextTime();
                result = new OptymoNextTime(optymoDirections[0].getLineNumber(), optymoDirections[0].getDirection(), optymoDirections[0].getStopName(), optymoDirections[0].getStopSlug(), nullResult);
            }

            return result;
        }

        @Override
        protected void onPostExecute(OptymoNextTime nextTime) {
            super.onPostExecute(nextTime);

            // we increase the number augmented favorites;
            numberOfUpdated++;
            if(nextTime != null)
                nextTimes.add(nextTime);

            // we possibly hide the reload button
            if(numberOfUpdated >= nextTimeRequests.size()) {
                refreshLayout.setRefreshing(false);

                Date date = new Date();
                String dateFormatted = dateFormat.format(date);
                lastUpdateText.setText(getResources().getString(R.string.main_last_updated, dateFormatted));
            }
            Log.d("nope", "Loaded favorite " + numberOfUpdated + "/" + nextTimeRequests.size() + " " + nextTime + " " + nextTimes.size());
//            Toast.makeText(MainActivity.this, "Loaded favorite " + numberOfUpdated + "/" + nextTimeRequests.size() + " " + nextTime, Toast.LENGTH_SHORT).show();

            // sort nextTimes by time
            Collections.sort(nextTimes);

            // update data
            favoritesAdapter.notifyDataSetChanged();
            MainActivity.this.updateListHeight();
        }
    }
}

package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.FavoritesController;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.vue.adapters.OptymoNextTimeAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNextTime;
import com.therolf.optymoNextModel.OptymoStop;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


@SuppressLint("StaticFieldLeak")
@SuppressWarnings("unused")
public class MainActivity extends TopViewActivity {

    private static OptymoNetworkController networkController = null;

    private ListView favoriteList;
    private SwipeRefreshLayout refreshLayout;
    private TextView lastUpdateText;

    int numberOfUpdated = 0;
    private ArrayList<OptymoGetNextTime> nextTimeRequests = new ArrayList<>();
    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();
    private OptymoNextTimeAdapter favoritesAdapter;
    private DateFormat dateFormat;

    private static Intent addFavoriteActivity = null;

    public static void destroyIntent() {
        addFavoriteActivity = null;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main, true);
        networkController = OptymoNetworkController.getInstance();
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
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                refreshFavoriteList();
            }
        });

        // favoriteList on long press delete element
        favoriteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final int pos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomDialog);
                builder.setCancelable(true);
                builder.setTitle(R.string.dialog_del_fav_title);
                builder.setMessage(getResources().getString(R.string.dialog_del_fav_message, nextTimes.get(pos).directionToString()));
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FavoritesController.getInstance(MainActivity.this).remove(nextTimes.get(pos), MainActivity.this);
                                refreshFavoriteList();
                            }
                        });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        // search icon
        findViewById(R.id.top_search_icon).setVisibility(View.VISIBLE);

        // bottom floating button
        com.robertlevonyan.views.customfloatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addFavoriteActivity == null) {
                    addFavoriteActivity = new Intent(MainActivity.this, FavoritesActivity.class);
                    startActivity(addFavoriteActivity);
                }
            }
        });
        /*fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN ) {
                    enableFloatingButton(false);
                }

                return true;
            }
        });*/
    }

    private void refreshFavoriteList() {
        Toast.makeText(this, R.string.main_toast_loading_favorites, Toast.LENGTH_SHORT).show();

        OptymoDirection[] directions = FavoritesController.getInstance(this).getFavorites();
//        System.out.println(Arrays.toString(directions));

        // if you have no favorites
        // BUG FIX no favorite infinite refreshing
        if(directions.length == 0) {
            refreshLayout.setRefreshing(false);

            // update data
            favoritesAdapter.notifyDataSetChanged();
            // update height
            Utility.setListViewHeightBasedOnChildren(favoriteList);
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

    class OptymoGetNextTime extends AsyncTask<OptymoDirection, Void, OptymoNextTime> {
        @Override
        protected OptymoNextTime doInBackground(OptymoDirection... optymoDirections) {
            OptymoNextTime result = null;

            OptymoStop stop = networkController.getStopByKey(optymoDirections[0].getStopSlug());
            if(stop != null) {
                OptymoNextTime[] nextTimes = stop.getNextTimes();

                int i = 0;
                while(i < nextTimes.length && result == null) {
                    if(nextTimes[i].getLineNumber() == optymoDirections[0].getLineNumber() && nextTimes[i].getDirection().equals(optymoDirections[0].getDirection()))
                        result = nextTimes[i];
                    ++i;
                }
            }

            // provide default result
            if(result == null)
                result = new OptymoNextTime(optymoDirections[0].getLineNumber(), optymoDirections[0].getDirection(), optymoDirections[0].getStopName(), optymoDirections[0].getStopSlug(), OptymoNextTime.NULL_TIME_VALUE);

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

            // update height
            Utility.setListViewHeightBasedOnChildren(favoriteList);
        }
    }
}

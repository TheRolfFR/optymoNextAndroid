package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.GlobalApplication;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.vue.adapters.NextTimeAdapter;
import com.therolf.optymoNextModel.OptymoNextTime;
import com.therolf.optymoNextModel.OptymoStop;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class StopActivity extends TopViewActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String STOP_SLUG_KEY = "stopslug_key";

    SwipeRefreshLayout refreshLayout;

    ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();
    NextTimeAdapter nextTimeItemAdapter;
    TextView errorTextView;
    ListView nextTimeListView;

    private String stopSlug;

    private StopGetNextTimes request;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_stop, false);
        this.refreshLayout = findViewById(R.id.stop_refresh_layout);
        this.errorTextView = findViewById(R.id.stop_error_textview);
        this.nextTimeListView = findViewById(R.id.stop_nextstops_listview);

        // refresh layout
        this.refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement); // color scheme

        // save slug
        this.stopSlug = getIntent().getStringExtra(STOP_SLUG_KEY);

        // decoding everything
        this.decode();
    }

    /**
     * Function decoding and loading next times
     */
    private void decode() {
        // if network generated and stopslug decoded
        if(((GlobalApplication) getApplication()).getNetworkController().isGenerated() && stopSlug != null) {
            // try to get the stop by its slug
            OptymoStop stop = ((GlobalApplication) getApplication()).getNetworkController().getStopBySlug(stopSlug);

            // if the stop exists
            if(stop != null) {

                // update the name
                ((TextView) findViewById(R.id.stop_main_title)).setText(stop.getName());

                // set the adapter to next times
                this.nextTimeItemAdapter = new NextTimeAdapter(this, nextTimes);
                this.nextTimeListView.setAdapter(this.nextTimeItemAdapter);

                // clear old next times
                nextTimes.clear();

                // cancel old request
                if(request != null) {
                    request.cancel(true);
                }

                // make a new one
                request = new StopGetNextTimes(this);
                request.execute(stop); // and execute it
            } else {
                this.refreshLayout.setRefreshing(false); // stop thinking
            }
        } else {
            this.refreshLayout.setRefreshing(false); // stop thinking
        }
    }

    /**
     * Pre-made static method to load this activity, easier to use outside
     * @param context the context to load the activity with
     * @param stopSlug the slug as parameter
     */
    public static void launchStopActivity(Activity context, String stopSlug) {
        Intent intent = new Intent(context, StopActivity.class);
        intent.putExtra(STOP_SLUG_KEY, stopSlug);
        context.startActivity(intent);
    }

    /**
     * Implementation of SwipeRefreshLayout.OnRefreshListener to refresh next times
     */
    @Override
    public void onRefresh() {
        this.refreshLayout.setRefreshing(true);
        this.decode();
    }

    /**
     * Request to get all next times
     */
    private static class StopGetNextTimes extends AsyncTask<OptymoStop, Void, OptymoNextTime[]> {

        private WeakReference<StopActivity> activityReference;

        StopGetNextTimes(StopActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected OptymoNextTime[] doInBackground(OptymoStop... optymoStops) {
            try {
                return optymoStops[0].getNextTimes();
            } catch (IOException e) {
                e.printStackTrace();

                // get activity
                StopActivity activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return new OptymoNextTime[0];

                activity.runOnUiThread(() -> {
                    // get a reference to the activity if it is still there
                    activity.errorTextView.setVisibility(View.VISIBLE);
                });
            }

            return new OptymoNextTime[0];
        }

        @Override
        protected void onPostExecute(OptymoNextTime[] optymoNextTimes) {
            super.onPostExecute(optymoNextTimes);

            // get activity
            StopActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.nextTimes.addAll(Arrays.asList(optymoNextTimes));
            Collections.sort(activity.nextTimes);

            activity.runOnUiThread(() -> {

                // make refreshlayout not refreshing
                activity.refreshLayout.setRefreshing(false);

                if(optymoNextTimes.length != 0) {
                    activity.errorTextView.setVisibility(View.GONE);
                }
            });

            activity.nextTimeItemAdapter.notifyDataSetChanged();
            Utility.setListViewHeightBasedOnChildren(activity.nextTimeListView);

        }
    }
}

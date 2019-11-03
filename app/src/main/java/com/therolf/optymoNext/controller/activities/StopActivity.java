package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.vue.adapters.OptymoNextTimeAdapter;
import com.therolf.optymoNextModel.OptymoNextTime;
import com.therolf.optymoNextModel.OptymoStop;

import java.util.ArrayList;
import java.util.Arrays;

public class StopActivity extends TopViewActivity {

    public static final String STOP_SLUG_KEY = "stopslug_key";

    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();
    private OptymoNextTimeAdapter nextTimeAdapter;
    private ListView nextTimeListView;

    private StopGetNextTimes request;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_line, false);
        boolean notFinishedWell = true;

        String stopSlug = getIntent().getStringExtra(STOP_SLUG_KEY);

        if(OptymoNetworkController.getInstance().isGenerated() && stopSlug != null) {
            OptymoStop stop = OptymoNetworkController.getInstance().getStopBySlug(stopSlug);

            if(stop != null) {
                notFinishedWell = false;

                ((TextView) findViewById(R.id.line_main_title)).setText(stop.getName());

                this.nextTimeListView = findViewById(R.id.line_stops_listview);
                this.nextTimeAdapter = new OptymoNextTimeAdapter(this, nextTimes);
                this.nextTimeListView.setAdapter(this.nextTimeAdapter);

                nextTimes.clear();

                // maybe future add refresh ?
                if(request != null) {
                    request.cancel(true);
                }

                request = new StopGetNextTimes();
                request.execute(stop);
                Toast.makeText(this, getString(R.string.stop_toast_loading_stop), Toast.LENGTH_SHORT).show();
            }
        }

        if(notFinishedWell)
            finish();
    }

    @SuppressWarnings("unused")
    public static void launchStopActivity(Activity context, String stopSlug) {
        Intent intent = new Intent(context, StopActivity.class);
        intent.putExtra(STOP_SLUG_KEY, stopSlug);
        context.startActivity(intent);
    }

    class StopGetNextTimes extends AsyncTask<OptymoStop, Void, OptymoNextTime[]> {
        @Override
        protected OptymoNextTime[] doInBackground(OptymoStop... optymoStops) {
            return optymoStops[0].getNextTimes();
        }

        @Override
        protected void onPostExecute(OptymoNextTime[] optymoNextTimes) {
            super.onPostExecute(optymoNextTimes);

            nextTimes.addAll(Arrays.asList(optymoNextTimes));

            nextTimeAdapter.notifyDataSetChanged();
            Utility.setListViewHeightBasedOnChildren(StopActivity.this.nextTimeListView);

        }
    }
}

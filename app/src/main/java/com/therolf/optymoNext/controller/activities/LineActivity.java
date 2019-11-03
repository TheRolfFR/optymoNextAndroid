package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.vue.adapters.OptymoStopAdapter;
import com.therolf.optymoNextModel.OptymoLine;

public class LineActivity extends TopViewActivity {

    public static final String LINE_NUMBER_KEY_DATA = "line_number_key";
    public static final String LINE_NAME_KEY_DATA = "line_name_key";
    private static final int LINE_NUMBER_DEFAULT_VALUE = 0;

    @SuppressLint({"MissingSuperCall", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_line, false);
        boolean notFinishedWell = true;

        String lineName = getIntent().getStringExtra(LINE_NAME_KEY_DATA);
        int lineNumber = getIntent().getIntExtra(LINE_NUMBER_KEY_DATA, LINE_NUMBER_DEFAULT_VALUE);

        if(OptymoNetworkController.getInstance().isGenerated() && lineNumber != LINE_NUMBER_DEFAULT_VALUE && lineName != null) {
            OptymoLine line = OptymoNetworkController.getInstance().getLineByNumberAndName(lineNumber, lineName);

            if(line != null) {
                notFinishedWell = false;

                ((TextView) findViewById(R.id.line_main_title)).setText("" + line.getNumber() + " - " + line.getName());

                ListView stopsListView = findViewById(R.id.line_stops_listview);
                stopsListView.setAdapter(new OptymoStopAdapter(this, line.getStops()));
                Utility.setListViewHeightBasedOnChildren(stopsListView);
            }
        }

        if(notFinishedWell) {
            Log.e("line", "I didn't find tour fucking line");
            finish();
        }
    }

    @SuppressWarnings("unused")
    public static void launchLineActivity(Activity context, int lineNumber, String lineName) {
        Log.d("line", lineNumber + " " + lineName);
        Intent intent = new Intent(context, LineActivity.class);
        intent.putExtra(LINE_NUMBER_KEY_DATA, lineNumber);
        intent.putExtra(LINE_NAME_KEY_DATA, lineName);
        context.startActivity(intent);
    }
}

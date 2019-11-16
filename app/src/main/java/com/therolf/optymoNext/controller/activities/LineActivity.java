package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.GlobalApplication;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.vue.adapters.StopAdapter;
import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoStop;

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

        if(((GlobalApplication) getApplication()).getNetworkController().isGenerated() && lineNumber != LINE_NUMBER_DEFAULT_VALUE && lineName != null) {
            OptymoLine line = ((GlobalApplication) getApplication()).getNetworkController().getLineByNumberAndName(lineNumber, lineName);

            if(line != null) {
                notFinishedWell = false;

                // update title
                ((TextView) findViewById(R.id.line_icon_number)).setText("" + line.getNumber());
                int colorId = getResources().getIdentifier("colorLine" + line.getNumber(), "color", getPackageName());
                findViewById(R.id.line_icon_bg).setBackgroundColor(ContextCompat.getColor(this, colorId));
                ((TextView) findViewById(R.id.line_main_title)).setText(line.getName());

                // update listview
                ListView stopsListView = findViewById(R.id.line_stops_listview);
                StopAdapter stopAdapter = new StopAdapter(this, line.getStops());
                stopsListView.setAdapter(stopAdapter);
                stopsListView.setOnItemClickListener((parent, view, position, id) -> {
                    Object o = stopAdapter.getItem(position);
//            Log.d("line", o.toString());
                    if(o instanceof OptymoStop) {
                        OptymoStop s = (OptymoStop) o;
//                Log.d("line", s.toString());
                        StopActivity.launchStopActivity(LineActivity.this, s.getSlug());
                    }
                });
                Utility.setListViewHeightBasedOnChildren(stopsListView);

                // scroll to top
                NestedScrollView nestedScrollView = findViewById(R.id.line_nestedscrollview);
                nestedScrollView.fullScroll(View.FOCUS_UP);
                nestedScrollView.fullScroll(View.FOCUS_UP);
            }
        }

        if(notFinishedWell) {
            Log.e("optymonext", "I didn't find your fucking line");
            finish();
        }
    }

    @SuppressWarnings("unused")
    public static void launchLineActivity(Activity context, int lineNumber, String lineName) {
//        Log.d("line", lineNumber + " " + lineName);
        Intent intent = new Intent(context, LineActivity.class);
        intent.putExtra(LINE_NUMBER_KEY_DATA, lineNumber);
        intent.putExtra(LINE_NAME_KEY_DATA, lineName);
        context.startActivity(intent);
    }
}

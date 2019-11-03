package com.therolf.optymoNext.controller;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.activities.LineActivity;
import com.therolf.optymoNext.controller.activities.StopActivity;
import com.therolf.optymoNext.vue.adapters.OptymoLineAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoStopAdapter;
import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoStop;

import java.util.ArrayList;

public class SearchController implements TextWatcher {
    Activity activity;

    private EditText searchInput;

    private ImageView searchIcon;
    private Drawable searchDrawable;
    private Drawable closeDrawable;

    private ArrayList<OptymoStop> stopsResultsList = new ArrayList<>();
    private OptymoStopAdapter stopsResultsAdapter;
    private ListView stopsResultListView;

    private ArrayList<OptymoLine> linesResultsList = new ArrayList<>();
    private OptymoLineAdapter linesResultsAdapter;
    private ListView linesResultListView;

    private boolean searching;

    @SuppressWarnings("unused")
    public SearchController(EditText searchInput, ImageView searchIcon, ListView linesResultListView, ListView stopsResultListView, Activity context) {
        this.activity = context;

        this.searchInput = searchInput;
        this.searchIcon = searchIcon;
        this.stopsResultListView = stopsResultListView;
        this.linesResultListView = linesResultListView;

        // set search / close drawable for image view
        this.searchDrawable = this.searchIcon.getDrawable();
        this.closeDrawable = context.getDrawable(R.drawable.ic_close_red);

        // add key listener for this
        this.searchInput.addTextChangedListener(this);

        // initialize adapters with data
        this.stopsResultsAdapter = new OptymoStopAdapter(context, new OptymoStop[0]);
        this.linesResultsAdapter = new OptymoLineAdapter(context, new OptymoLine[0]);

        // set adapters to lists
        this.stopsResultListView.setAdapter(this.stopsResultsAdapter);
        this.linesResultListView.setAdapter(this.linesResultsAdapter);

        // set on item click listeners
        this.stopsResultListView.setOnItemClickListener((parent, view, position, id) -> {
            Object o = SearchController.this.stopsResultsAdapter.getItem(position);
//            Log.d("search", o.toString());
            if(o instanceof OptymoStop) {
                OptymoStop s = (OptymoStop) o;
//                Log.d("search", s.toString());
                StopActivity.launchStopActivity(this.activity, s.getSlug());
            }
        });
        this.linesResultListView.setOnItemClickListener((parent, view, position, id) -> {
            Object o = SearchController.this.linesResultsAdapter.getItem(position);
            Log.d("search", o.toString());
            if(o instanceof OptymoLine) {
                OptymoLine l = (OptymoLine) o;
                Log.d("search", l.toString());
                LineActivity.launchLineActivity(this.activity, l.getNumber(), l.getName());
            }
        });

        // and hide completely
        this.stopsResultListView.setVisibility(View.GONE);
        this.linesResultListView.setVisibility(View.GONE);

        // set icon click adapter
        this.searchIcon.setOnClickListener(view -> {
            if(this.searchInput.getText().length() > 0)
                this.searchInput.setText("");
        });
    }

    private void search(String search) {
        if(!searching) {
            search = search.toLowerCase();
//            Log.d("search", search);
            searching = true;

            if (search.length() > 1 || (search.length() == 1 && search.substring(0, 1).matches("\\d"))) {
                // change icon drawable according to input
                searchIcon.setImageDrawable(closeDrawable);

                // if the network is generated
                if (OptymoNetworkController.getInstance().isGenerated()) {
//                    Log.d("search", "generated network");
                    // empty the previous array list
                    this.stopsResultsList.clear();
                    // get the stops starting with search
                    OptymoStop[] stops = OptymoNetworkController.getInstance().getStops();
                    for (OptymoStop s : stops) {
                        if (s.getName().toLowerCase().startsWith(search))
                            this.stopsResultsList.add(s);
                    }

                    OptymoStop[] stopResults = this.stopsResultsList.toArray(new OptymoStop[0]);
                    this.stopsResultsAdapter.setData(stopResults);
                    if(stopResults.length > 0)
                        this.stopsResultListView.setVisibility(View.VISIBLE);
                    else
                        this.stopsResultListView.setVisibility(View.GONE);

//                    Log.d("search", "found " + this.stopsResults.length + " stops");

                    // empty the previous array list
                    this.linesResultsList.clear();

                    // get the line starting with search
                    OptymoLine[] lines = OptymoNetworkController.getInstance().getLines();
                    for (OptymoLine l : lines) {
                        if (l.getName().toLowerCase().contains(search) || (search.length() == 1 && search.substring(0, 1).matches("\\d") && search.charAt(0) - '0' == l.getNumber()))
                            this.linesResultsList.add(l);
                    }

                    OptymoLine[] lineResults = this.linesResultsList.toArray(new OptymoLine[0]);
                    this.linesResultsAdapter.setData(lineResults);
                    if(lineResults.length > 0)
                        this.linesResultListView.setVisibility(View.VISIBLE);
                    else
                        this.linesResultListView.setVisibility(View.GONE);

//                    Log.d("search", "found " + this.stopsResults.length + " lines");

                    // out of the if it will update the adapters
                }
            } else {
                // change icon drawable according to input
                searchIcon.setImageDrawable(searchDrawable);

                // empty results
                this.linesResultsAdapter.setData(new OptymoLine[0]);
                this.stopsResultsAdapter.setData(new OptymoStop[0]);

                // hide completely
                this.stopsResultListView.setVisibility(View.GONE);
                this.linesResultListView.setVisibility(View.GONE);
            }

            this.linesResultsAdapter.notifyDataSetChanged();
            this.stopsResultsAdapter.notifyDataSetChanged();

            // change utility size
            Utility.setListViewHeightBasedOnChildren(this.linesResultListView);
            Utility.setListViewHeightBasedOnChildren(this.stopsResultListView);

            searching = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        search(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {}
}

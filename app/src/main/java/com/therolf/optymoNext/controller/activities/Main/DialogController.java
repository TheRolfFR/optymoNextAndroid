package com.therolf.optymoNext.controller.activities.Main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.NetworkController;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.controller.activities.LineActivity;
import com.therolf.optymoNext.controller.activities.StopActivity;
import com.therolf.optymoNext.vue.adapters.LineAdapter;
import com.therolf.optymoNext.vue.adapters.StopAdapter;
import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoStop;

import java.text.Normalizer;
import java.util.ArrayList;

public class DialogController implements TextWatcher {
    private Activity activity;

    private EditText searchInput;

    private ImageView searchIcon;
    private Drawable searchDrawable;
    private Drawable closeDrawable;

    private LinearLayout inputBackground;
    private Drawable normalInputBackgroundDrawable;
    private Drawable searchInputTopBackgroundDrawable;

    private NestedScrollView searchResultsView;

    private LinearLayout stopsResultLayout;
    private ArrayList<OptymoStop> stopsResultsList = new ArrayList<>();
    private StopAdapter stopsResultsAdapter;
    private ListView stopsResultListView;

    private LinearLayout linesResultLayout;
    private ArrayList<OptymoLine> linesResultsList = new ArrayList<>();
    private LineAdapter linesResultsAdapter;
    private ListView linesResultListView;

    private TextView noResultsFoundTextView;

    private boolean searching;
    @SuppressWarnings("FieldCanBeLocal")
    private boolean resultFound;

    @SuppressWarnings("unused")
    public DialogController(Activity context) {

        // search part
        Dialog searchDialog = new Dialog(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                // Tap anywhere to close dialog.
                Rect dialogBounds = new Rect();
                Window window = this.getWindow();
                InputMethodManager inputMethodManager = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if(window != null && inputMethodManager != null) {
                    window.getDecorView().getHitRect(dialogBounds);
                    if (!dialogBounds.contains((int) event.getX(), (int) event.getY())) {
                        // You have clicked the grey area
                        View v = this.getCurrentFocus();
                        if(v != null) {
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    } else {
                        View v = this.getCurrentFocus();
                        if(v != null) {
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            inputMethodManager.hideSoftInputFromWindow(this
                                    .getCurrentFocus().getWindowToken(), 0);
                        }
                        this.dismiss();
                    }
                }

                return true;
            }
        };
        searchDialog.setCanceledOnTouchOutside(true);
        Utility.setMargins(searchDialog, 0, 0, 0, 0);
        searchDialog.setContentView(R.layout.dialog_search);
        Window window = searchDialog.getWindow();
        if(window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }

        this.activity = context;

        this.searchInput = searchDialog.findViewById(R.id.dialog_search_search_input);
        this.searchIcon = searchDialog.findViewById(R.id.dialog_search_search_or_remove_button);
        this.searchResultsView = searchDialog.findViewById(R.id.dialog_search_results);
        this.stopsResultLayout = searchDialog.findViewById(R.id.dialog_search_stops_results);
        this.linesResultLayout = searchDialog.findViewById(R.id.dialog_search_lines_results);
        this.stopsResultListView = searchDialog.findViewById(R.id.dialog_search_stop_list_view);
        this.linesResultListView = searchDialog.findViewById(R.id.dialog_search_line_list_view);

        // set search / close drawable for image view
        this.searchDrawable = this.searchIcon.getDrawable();
        this.closeDrawable = context.getDrawable(R.drawable.ic_close_red);

        // get background drawable
        this.inputBackground = searchDialog.findViewById(R.id.dialog_search_input_background);
        this.normalInputBackgroundDrawable = context.getDrawable(R.drawable.element_background);
        this.searchInputTopBackgroundDrawable = context.getDrawable(R.drawable.element_background_toppart);

        // add key listener for this
        this.searchInput.addTextChangedListener(this);
        // initialize adapters with data
        this.stopsResultsAdapter = new StopAdapter(context, new OptymoStop[0]);
        this.linesResultsAdapter = new LineAdapter(context, new OptymoLine[0]);

        // set adapters to lists
        this.stopsResultListView.setAdapter(this.stopsResultsAdapter);
        this.linesResultListView.setAdapter(this.linesResultsAdapter);

        // set on item click listeners
        this.stopsResultListView.setOnItemClickListener((parent, view, position, id) -> {
            Object o = DialogController.this.stopsResultsAdapter.getItem(position);
//            Log.d("search", o.toString());
            if(o instanceof OptymoStop) {
                OptymoStop s = (OptymoStop) o;
//                Log.d("search", s.toString());
                StopActivity.launchStopActivity(this.activity, s.getSlug());
            }
        });
        this.linesResultListView.setOnItemClickListener((parent, view, position, id) -> {
            Object o = DialogController.this.linesResultsAdapter.getItem(position);
//            Log.d("search", o.toString());
            if(o instanceof OptymoLine) {
                OptymoLine l = (OptymoLine) o;
//                Log.d("search", l.toString());
                LineActivity.launchLineActivity(this.activity, l.getNumber(), l.getName());
            }
        });

        this.noResultsFoundTextView = searchDialog.findViewById(R.id.dialog_search_no_results_found);

        // and hide completely
        this.searchResultsView.setVisibility(View.GONE);
        this.stopsResultLayout.setVisibility(View.GONE);
        this.stopsResultLayout.setVisibility(View.GONE);

        // set icon click adapter
        this.searchIcon.setOnClickListener(view -> {
            if(this.searchInput.getText().length() > 0)
                this.searchInput.setText("");
        });

        // show dialog
        searchDialog.show();

        // search input auto focus
        EditText editText = this.searchInput;
        editText.setOnFocusChangeListener((v, hasFocus) -> editText.post(() -> {
            InputMethodManager inputMethodManager= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }));
        editText.requestFocus();
    }

    private void search(String search) {
        if(!searching) {
            search = search.toLowerCase();
            search = Normalizer.normalize(search, Normalizer.Form.NFD);
            search = search.replaceAll("[^\\p{ASCII}]", "");
//            Log.d("search", search);
            searching = true;
            resultFound = false;

            if (search.length() > 0) {
                // change icon drawable according to input
                searchIcon.setImageDrawable(closeDrawable);

                // if the network is generated
                if (NetworkController.getInstance().isGenerated()) {
//                    Log.d("search", "generated network");
                    // empty the previous array list
                    this.stopsResultsList.clear();
                    // get the stops starting with search
                    OptymoStop[] stops = NetworkController.getInstance().getStops();
                    String stopName;
                    for (OptymoStop s : stops) {
                        stopName = Normalizer.normalize(s.getName(), Normalizer.Form.NFD);
                        stopName = stopName.replaceAll("[^\\p{ASCII}]", "");
                        if (stopName.toLowerCase().startsWith(search))
                            this.stopsResultsList.add(s);
                    }

                    OptymoStop[] stopResults = this.stopsResultsList.toArray(new OptymoStop[0]);
                    this.stopsResultsAdapter.setData(stopResults);
                    if(stopResults.length > 0)
                        this.stopsResultLayout.setVisibility(View.VISIBLE);
                    else
                        this.stopsResultLayout.setVisibility(View.GONE);

                    // we found a result
                    if(this.stopsResultsList.size() > 0)
                        resultFound = true;

                    // empty the previous array list
                    this.linesResultsList.clear();

                    // get the line starting with search
                    OptymoLine[] lines = NetworkController.getInstance().getLines();
                    String lineName;
                    for (OptymoLine l : lines) {
                        // smart line search: search lines with name containing the seach or ligne with the same number
                        lineName = Normalizer.normalize(l.getName(), Normalizer.Form.NFD);
                        lineName = lineName.replaceAll("[^\\p{ASCII}]", "");
                        if (lineName.toLowerCase().startsWith(search) || (search.length() == 1 && search.substring(0, 1).matches("\\d") && search.charAt(0) - '0' == l.getNumber()))
                            this.linesResultsList.add(l);
                    }

                    OptymoLine[] lineResults = this.linesResultsList.toArray(new OptymoLine[0]);
                    this.linesResultsAdapter.setData(lineResults);
                    if(lineResults.length > 0)
                        this.linesResultLayout.setVisibility(View.VISIBLE);
                    else
                        this.linesResultLayout.setVisibility(View.GONE);

//                    Log.d("search", "found " + this.stopsResults.length + " lines");
                    if(!resultFound && this.linesResultsList.size() > 0)
                        resultFound = true;

                    // out of the if it will update the adapters
                }
            } else {
                // change icon drawable according to input
                searchIcon.setImageDrawable(searchDrawable);

                // empty results
                this.linesResultsAdapter.setData(new OptymoLine[0]);
                this.stopsResultsAdapter.setData(new OptymoStop[0]);

                // hide completely
                this.stopsResultLayout.setVisibility(View.GONE);
                this.linesResultLayout.setVisibility(View.GONE);

                // change bg and search view result
                inputBackground.setBackground(normalInputBackgroundDrawable);
                searchResultsView.setVisibility(View.GONE);
            }

            this.linesResultsAdapter.notifyDataSetChanged();
            this.stopsResultsAdapter.notifyDataSetChanged();

            // if there is any result show it
            if(resultFound) {
                inputBackground.setBackground(searchInputTopBackgroundDrawable);
                searchResultsView.setVisibility(View.VISIBLE);
                noResultsFoundTextView.setVisibility(View.GONE);
            } else {
                noResultsFoundTextView.setVisibility(View.VISIBLE);
            }

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

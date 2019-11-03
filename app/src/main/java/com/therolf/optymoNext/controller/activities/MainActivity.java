package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.FavoritesController;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.controller.SearchController;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNext.controller.activities.Main.SnackBarController;
import com.therolf.optymoNext.controller.notifications.NotificationController;
import com.therolf.optymoNext.vue.adapters.OptymoNextTimeAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;


@SuppressLint("StaticFieldLeak")
@SuppressWarnings("unused")
public class MainActivity extends TopViewActivity {

    private NotificationController notificationController = new NotificationController();

    private ListView favoriteList;
    private SwipeRefreshLayout refreshLayout;
    private TextView lastUpdateText;

    // favorites
    int numberOfUpdated = 0;
    private ArrayList<OptymoGetNextTime> nextTimeRequests = new ArrayList<>();
    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();
    private OptymoNextTimeAdapter favoritesAdapter;
    private DateFormat dateFormat;

    private Dialog searchDialog;

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

        // notifications (BEFORE refreshFavoriteList)
        notificationController.run(this);

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
            builder.setPositiveButton(R.string.dialog_yes,
                    (dialog, which) -> {
                        FavoritesController.getInstance().remove(nextTimes.get(pos), MainActivity.this);
                        refreshFavoriteList();
                    });
            builder.setNegativeButton(R.string.dialog_no, (dialog, which) -> {
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        });

        // search icon
        findViewById(R.id.top_search_icon).setVisibility(View.VISIBLE);

        // bottom floating button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(view -> {
            if(addFavoriteActivity == null) {
                addFavoriteActivity = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(addFavoriteActivity);
            }
        });

        // search part
        searchDialog = new Dialog(this);
        setMargins(searchDialog, 0, 0, 0, 0);
        searchDialog.setContentView(R.layout.dialog_search);
        Window window = searchDialog.getWindow();
        if(window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.TOP;
            window.setAttributes(wlp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        // search part
        ImageView searchButton = findViewById(R.id.top_search_icon);
        searchButton.setEnabled(false);
        searchButton.setOnClickListener(view -> {
            searchDialog.show();
            searchDialog.findViewById(R.id.dialog_search_search_input).setFocusableInTouchMode(true);
            searchDialog.findViewById(R.id.dialog_search_search_input).requestFocus();
        });
        // initialize search controller
        SearchController searchController = new SearchController(
                searchDialog.findViewById(R.id.dialog_search_search_input),
                searchDialog.findViewById(R.id.dialog_search_search_or_remove_button),
                searchDialog.findViewById(R.id.dialog_search_line_list_view),
                searchDialog.findViewById(R.id.dialog_search_stop_list_view),
                this);

        // snackbar
        SnackBarController.run(this, searchButton, fab);
    }

    private void refreshFavoriteList() {
        Toast.makeText(this, R.string.main_toast_loading_favorites, Toast.LENGTH_SHORT).show();

        OptymoDirection[] directions = FavoritesController.getInstance().getFavorites();
        System.out.println(Arrays.toString(directions));

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

        // empty notification body
        notificationController.resetNotificationBody();
        // change title
        notificationController.updateTitle(this.getString(R.string.update_pending));

        // tell user that update is pending
        lastUpdateText.setText(getResources().getString(R.string.update_pending));

        // we add all the new favorites
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
                lastUpdateText.setText(getResources().getString(R.string.update_last, dateFormatted));
                notificationController.setUpdatedAtTitle(MainActivity.this);
            }
            Log.d("nope", "Loaded favorite " + numberOfUpdated + "/" + nextTimeRequests.size() + " " + nextTime + " " + nextTimes.size());
//            Toast.makeText(MainActivity.this, "Loaded favorite " + numberOfUpdated + "/" + nextTimeRequests.size() + " " + nextTime, Toast.LENGTH_SHORT).show();

            // sort nextTimes by time
            Collections.sort(nextTimes);
            notificationController.updateBody(nextTimes);

            // update data
            favoritesAdapter.notifyDataSetChanged();
            MainActivity.this.updateListHeight();
        }
    }


    @SuppressLint("RtlHardcoded")
    public static Dialog setMargins(Dialog dialog, int marginLeft, int marginTop, int marginRight, int marginBottom )
    {
        Window window = dialog.getWindow();
        if ( window == null )
        {
            // dialog window is not available, cannot apply margins
            return dialog;
        }
        Context context = dialog.getContext();

        // set dialog to fullscreen
        RelativeLayout root = new RelativeLayout( context );
        root.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT ) );
        dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        dialog.setContentView( root );
        // set background to get rid of additional margins
        window.setBackgroundDrawable( new ColorDrawable( Color.WHITE ) );

        // apply left and top margin directly
        window.setGravity( Gravity.LEFT | Gravity.TOP );
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.x = marginLeft;
        attributes.y = marginTop;
        window.setAttributes( attributes );

        // set right and bottom margin implicitly by calculating width and height of dialog
        Point displaySize = getDisplayDimensions( context );
        int width = displaySize.x - marginLeft - marginRight;
        int height = displaySize.y - marginTop - marginBottom;
        window.setLayout( width, height );

        return dialog;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static Point getDisplayDimensions( Context context )
    {
        WindowManager wm = ( WindowManager ) context.getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics( metrics );
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // find out if status bar has already been subtracted from screenHeight
        display.getRealMetrics( metrics );
        int physicalHeight = metrics.heightPixels;
        int statusBarHeight = getStatusBarHeight( context );
        int navigationBarHeight = getNavigationBarHeight( context );
        int heightDelta = physicalHeight - screenHeight;
        if ( heightDelta == 0 || heightDelta == navigationBarHeight )
        {
            screenHeight -= statusBarHeight;
        }

        return new Point( screenWidth, screenHeight );
    }

    public static int getStatusBarHeight( Context context )
    {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "status_bar_height", "dimen", "android" );
        return ( resourceId > 0 ) ? resources.getDimensionPixelSize( resourceId ) : 0;
    }

    public static int getNavigationBarHeight( Context context )
    {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "navigation_bar_height", "dimen", "android" );
        return ( resourceId > 0 ) ? resources.getDimensionPixelSize( resourceId ) : 0;
    }
}

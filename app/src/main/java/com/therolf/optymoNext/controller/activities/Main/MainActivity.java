package com.therolf.optymoNext.controller.activities.Main;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.activities.FavoritesActivity;
import com.therolf.optymoNext.controller.activities.Map.MapActivity;
import com.therolf.optymoNext.controller.activities.TopViewActivity;
import com.therolf.optymoNext.controller.global.AlertController;
import com.therolf.optymoNext.controller.global.GlobalApplication;
import com.therolf.optymoNext.controller.global.NetworkController;
import com.therolf.optymoNext.controller.global.Utility;
import com.therolf.optymoNext.controller.notifications.NotificationService;
import com.therolf.optymoNext.vue.adapters.LineNextTimeAdapter;
import com.therolf.optymoNext.vue.adapters.LinePdfAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public class MainActivity extends TopViewActivity {

    private ListView favoriteList;
    private SwipeRefreshLayout refreshLayout;
    private TextView lastUpdateText;

    private DialogController dialogController;

    // favorites
    private int numberOfUpdated = 0;
    private ArrayList<NextTimeRequest> nextTimeRequests = new ArrayList<>();
    private LineNextTimeAdapter favoritesAdapter;
    private DateFormat dateFormat;

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
        NetworkController networkController = ((GlobalApplication) getApplication()).getNetworkController();

        // favorite list (BEFORE refreshFavoriteList)
        favoriteList = findViewById(R.id.main_favorite_next_stops);
        favoritesAdapter = new LineNextTimeAdapter(this, favoriteList);
        favoriteList.setAdapter(favoritesAdapter);

        // refresh layout
        refreshLayout = findViewById(R.id.main_refresh_layout);
        refreshLayout.setColorSchemeResources(
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement);

        // last updated text (BEFORE refreshFavoriteList)
        lastUpdateText = findViewById(R.id.main_favorites_last_update_text);

        // date format
        dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);


        // add favorite button
        ImageButton addButton = findViewById(R.id.main_add_button);
        addButton.setEnabled(false);
        addButton.setOnClickListener(v -> {
            if(addFavoriteActivity == null) {
                addFavoriteActivity = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(addFavoriteActivity);
            }
        });

        // refresh button
        findViewById(R.id.main_refresh_button).setOnClickListener(v -> {
            refreshLayout.setRefreshing(true);
            refreshFavoriteList();
        });

        refreshFavoriteList();
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);
            refreshFavoriteList();
        });

        // favoriteList on long press delete element
        favoritesAdapter.setOnItemLongClickListener(nextTime -> {
            if(!(nextTime instanceof OptymoNextTime))return;

            OptymoNextTime n = (OptymoNextTime) nextTime;

            AlertController builder = new AlertController(MainActivity.this);
            builder.setTitle(R.string.dialog_del_fav_title);
            builder.setMessage(getResources().getString(R.string.dialog_del_fav_message, n.directionToString()));
            builder.setPositiveButton(R.string.dialog_yes,
                    (DialogInterface dialog, int which) -> {
                        ((GlobalApplication) getApplication()).getFavoritesController().remove(n, MainActivity.this);
                        refreshFavoriteList();
                    });
            builder.setNegativeButton(R.string.dialog_no, (dialog, which) -> {
            });

            builder.show();
        });

        // search icon
        findViewById(R.id.top_search_icon).setVisibility(View.VISIBLE);

        // bottom floating button
        // go to map
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });


        // search part
        ImageView searchButton = findViewById(R.id.top_search_icon);
        searchButton.setEnabled(false);
        searchButton.setOnClickListener(view -> dialogController = new DialogController(MainActivity.this));

        // snackbar
        SnackBarController.run(this, searchButton, fab, addButton);

        GridView gridView = findViewById(R.id.main_lines_pdf_gridview);
        LinePdfAdapter.LinePdf[] arr = new LinePdfAdapter.LinePdf[] {
                new LinePdfAdapter.LinePdf("" + 1, "https://www.optymo.fr/wp-content/uploads/2019/07/fiche_web_ligne-1_2019_09.pdf"),
                new LinePdfAdapter.LinePdf("" + 2, "https://www.optymo.fr/wp-content/uploads/2019/07/fiche_web_ligne-2_2019_09.pdf"),
                new LinePdfAdapter.LinePdf("" + 3, "https://www.optymo.fr/wp-content/uploads/2019/10/fiche_web_ligne-3_2019_08.pdf"),
                new LinePdfAdapter.LinePdf("" + 4, "https://www.optymo.fr/wp-content/uploads/2019/07/fiche_web_ligne_4_2019_08.pdf"),
                new LinePdfAdapter.LinePdf("" + 5, "https://www.optymo.fr/wp-content/uploads/2019/07/fiche_web_ligne_5_2019_08.pdf"),
                new LinePdfAdapter.LinePdf("" + 8, "https://www.optymo.fr/wp-content/uploads/2019/07/fiche_web_ligne_8_2019_08.pdf"),
                new LinePdfAdapter.LinePdf("" + 9, "https://www.optymo.fr/wp-content/uploads/2019/07/fiche_web_ligne_9_2019_08.pdf")
        };
        gridView.setAdapter(new LinePdfAdapter(arr, this));
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Uri uri = Uri.parse(arr[position].getPdfUrl());
            try
            {
                Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                intentUrl.setDataAndType(uri, "application/pdf");
                intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentUrl);
            }
            catch (ActivityNotFoundException e)
            {
                AlertController builder = new AlertController(MainActivity.this);
                builder.setTitle(R.string.dialog_download_pdf_title);
                builder.setMessage(R.string.dialog_download_pdf_content);
                builder.setPositiveButton(R.string.dialog_yes,
                        (dialog, which) -> {
                            Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                            intentUrl.setData(uri);
                            startActivity(intentUrl);
                        });
                builder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                });
                builder.show();
            }
        });

        // traffic info
        new TrafficController(this);

        // made by part
        TextView madeBy = findViewById(R.id.main_made_by);
        String s = getString(R.string.main_made_by_text) + "\n" + getString(R.string.main_made_by_website);
        SpannableString ss = new SpannableString(s);
        ClickableSpan website = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.main_made_by_website)));
                startActivity(intent);
            }
        };
        ss.setSpan(website, getString(R.string.main_made_by_text).length(), s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), getString(R.string.main_made_by_text).length(), s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        madeBy.setText(ss);
        madeBy.setMovementMethod(LinkMovementMethod.getInstance());

        new NotificationCheckboxController(this);
    }

    private void refreshFavoriteList() {
        OptymoDirection[] directions = ((GlobalApplication) getApplication()).getFavoritesController().getFavorites();

        Intent refreshIntent = new Intent(this.getApplicationContext(), NotificationService.class);
        refreshIntent.setAction(NotificationService.REFRESH_ACTION);
        startService(refreshIntent);

        // if you have no favorites
        // BUG FIX no favorite infinite refreshing
        if(directions.length == 0) {
            refreshLayout.setRefreshing(false);

            // update data
            favoritesAdapter.notifyDataSetChanged();
            // update height
            this.updateListHeight();

            lastUpdateText.setText(getResources().getString(R.string.no_favorites));
        } else {
            // tell user that update is pending
            lastUpdateText.setText(getResources().getString(R.string.update_pending));
        }

        // we reset number of updated
        numberOfUpdated = 0;
        favoritesAdapter.clear();

        // we add all the new favorites
        // 1. Stop the remaining requests
        NextTimeRequest tmp;
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
            tmp = new NextTimeRequest(this);
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

    private static class NextTimeRequest extends AsyncTask<OptymoDirection, Void, OptymoNextTime> {

        private WeakReference<MainActivity> reference;

        NextTimeRequest(MainActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        @Override
        protected OptymoNextTime doInBackground(OptymoDirection... optymoDirections) {
            OptymoNextTime result = null;
            OptymoNextTime latestResult = null;

            OptymoNextTime[]nextTimes = new OptymoNextTime[0];
            try {
                nextTimes = OptymoNetwork.getNextTimes(optymoDirections[0].getStopSlug());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i = 0;
            while(i < nextTimes.length && result == null) {
//                Log.d("OptymoNext", optymoDirections[0].toString() + " " + nextTimes[i].directionToString());
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
                    nullResult = OptymoNextTime.buildGreaterString(latestResult);
                result = new OptymoNextTime(optymoDirections[0].getLineNumber(), optymoDirections[0].getDirection(), optymoDirections[0].getStopName(), optymoDirections[0].getStopSlug(), nullResult);
            }

            return result;
        }

        @Override
        protected void onPostExecute(OptymoNextTime nextTime) {
            super.onPostExecute(nextTime);

            // get activity
            MainActivity activity = reference.get();
            if (activity == null || activity.isFinishing()) return;

            // we increase the number augmented favorites;
            activity.numberOfUpdated++;
            if(nextTime != null)
                activity.favoritesAdapter.addNextTime(activity, nextTime);

            // we possibly hide the reload button
            if(activity.numberOfUpdated >= activity.nextTimeRequests.size()) {
                activity.refreshLayout.setRefreshing(false);

                Date date = new Date();
                String dateFormatted = activity.dateFormat.format(date);
                activity.lastUpdateText.setText(activity.getResources().getString(R.string.update_last, dateFormatted));
            }
        }
    }
}

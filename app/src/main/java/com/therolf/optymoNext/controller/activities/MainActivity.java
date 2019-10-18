package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoFavoritesController;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.vue.adapters.OptymoDirectionAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoNextTimeAdapter;
import com.therolf.optymoNextModel.OptymoNextTime;


@SuppressLint("StaticFieldLeak")
@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {

    private static OptymoNetworkController networkController = null;

    ListView favoriteList;
    SwipeRefreshLayout refreshLayout;
    OptymoGetNextTime getNextTime = null;

    private static Intent addFavoriteActivity = null;

    private OptymoFavoritesController favoriteManager;

    public static void destroyIntent() {
        addFavoriteActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // data part (BEFORE REFRESH LAYOUT)
        favoriteManager   = OptymoFavoritesController.getInstance(this);
        networkController = OptymoNetworkController.getInstance();
        Log.e("lines number", "" + networkController.getLines().length);

        // favorite list (BEFORE REFRESH LAYOUT)
        favoriteList = findViewById(R.id.main_favorite_next_stops);

        // refresh layout
        refreshLayout = findViewById(R.id.main_refresh_layout);
        refreshLayout.setColorSchemeResources(
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement,
                R.color.colorPrimaryOnElement);
        refreshFavoriteList();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                refreshFavoriteList();
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
        Toast.makeText(this, "Loading favorites...", Toast.LENGTH_SHORT).show();
        favoriteList.setAdapter(new OptymoDirectionAdapter(this, favoriteManager.getFavorites()));
        refreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        Intent i = new Intent();
        i.putExtra("HELLO_WORLD", "!");
        setResult(2, i);
        super.onDestroy();
    }

    class OptymoGetNextTime extends AsyncTask<Integer, Void, OptymoNextTime[]> {
        @Override
        protected OptymoNextTime[] doInBackground(Integer... integers) {
            return networkController.getStops()[integers[0]].getNextTimes();
        }

        @Override
        protected void onPostExecute(OptymoNextTime[] optymoNextTimes) {
            super.onPostExecute(optymoNextTimes);

            favoriteList.setAdapter(new OptymoNextTimeAdapter(MainActivity.this, optymoNextTimes));
        }
    }
}

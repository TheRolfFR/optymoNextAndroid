package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoFavoritesController;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.vue.adapters.OptymoDirectionAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoNextTimeAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoStopAdapter;
import com.therolf.optymoNextModel.OptymoNextTime;


@SuppressLint("StaticFieldLeak")
@SuppressWarnings("unused")
public class MainActivity extends AppCompatActivity {

    private static OptymoNetworkController networkController = null;

    ImageButton favoriteRefreshButton;
    ListView favoriteList;

    ImageButton stopsRefreshButton;
    Spinner stopsSpinner;
    ListView nextStopsList;
    OptymoGetNextTime getNextTime = null;

    private OptymoFavoritesController favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // data part
        favoriteManager   = OptymoFavoritesController.getInstance(this);
        networkController = OptymoNetworkController.getInstance();
        Log.e("lines number", "" + networkController.getLines().length);

        // favorite parts
        favoriteRefreshButton = findViewById(R.id.main_favorite_refresh_button);
        favoriteList = findViewById(R.id.main_favorite_next_stops);

        // favorite set adapters
        favoriteRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(favoriteManager.getFavorites().length);
                favoriteList.setAdapter(new OptymoDirectionAdapter(MainActivity.this, favoriteManager.getFavorites()));
            }
        });
        favoriteList.setAdapter(new OptymoDirectionAdapter(this, favoriteManager.getFavorites()));


        // stops parts
        stopsSpinner = findViewById(R.id.main_stops_spinner);
        nextStopsList = findViewById(R.id.main_next_stops);

        // stops set data
        stopsSpinner.setAdapter(new OptymoStopAdapter(MainActivity.this, networkController.getStops()));
        stopsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(getNextTime != null) {
                    getNextTime.cancel(true);
                }

                getNextTime = new OptymoGetNextTime();
                getNextTime.execute(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                nextStopsList.setAdapter(new OptymoNextTimeAdapter(MainActivity.this, new OptymoNextTime[0]));
            }
        });

        // bottom floating button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addActivity = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(addActivity);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class OptymoGetNextTime extends AsyncTask<Integer, Void, OptymoNextTime[]> {
        @Override
        protected OptymoNextTime[] doInBackground(Integer... integers) {
            return networkController.getStops()[integers[0]].getNextTimes();
        }

        @Override
        protected void onPostExecute(OptymoNextTime[] optymoNextTimes) {
            super.onPostExecute(optymoNextTimes);

            nextStopsList.setAdapter(new OptymoNextTimeAdapter(MainActivity.this, optymoNextTimes));
        }
    }
}

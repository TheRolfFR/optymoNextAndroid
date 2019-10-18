package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.OptymoFavoritesController;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.controller.async.AsyncListener;
import com.therolf.optymoNext.vue.adapters.OptymoDirectionAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoStopAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoStop;

import java.util.Arrays;

public class FavoritesActivity extends AppCompatActivity {

    private Spinner directionSpinner;

    private OptymoStop[] stops = null;
    private OptymoDirection[] directions = null;

    private OptymoDirectionRequest directionRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favorite);

        stops = OptymoNetworkController.getInstance().getStops();

        Spinner stopSpinner = findViewById(R.id.add_stop_spinner);
        directionSpinner = findViewById(R.id.add_direction_spinner);
        Button addButton = findViewById(R.id.add_button);

        stopSpinner.setAdapter(new OptymoStopAdapter(this, stops));

        stopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(directionRequest != null) {
                    directionRequest.cancel(true);
                }

                directionRequest = new OptymoDirectionRequest(new AsyncListener() {
                    @Override
                    public void OnRequestEnd(Object result) {
                        directions = (OptymoDirection[]) result;
                        setAdapter(directions);
                    }
                });

                directionRequest.execute(stops[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(directions != null && directions.length != 0 && directionSpinner.getSelectedItemPosition() < directions.length) {
                    OptymoFavoritesController.getInstance(FavoritesActivity.this).addElement(directions[directionSpinner.getSelectedItemPosition()]);
                    Toast.makeText(FavoritesActivity.this, "Saved favorite!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void setAdapter(OptymoDirection[] directions) {
        System.err.println(Arrays.toString(directions));
        if(directions != null)
            directionSpinner.setAdapter(new OptymoDirectionAdapter(this, directions));
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("unused")
    class OptymoDirectionRequest extends AsyncTask<OptymoStop, Void, OptymoDirection[]> {

        private AsyncListener listener;

        OptymoDirectionRequest(AsyncListener listener) {
            this.listener = listener;
        }

        @Override
        protected OptymoDirection[] doInBackground(OptymoStop... optymoStops) {
            return optymoStops[0].getAvailableDirections();
        }

        @Override
        protected void onPostExecute(OptymoDirection[] optymoDirections) {
            super.onPostExecute(optymoDirections);
            listener.OnRequestEnd(optymoDirections);
        }
    }
}

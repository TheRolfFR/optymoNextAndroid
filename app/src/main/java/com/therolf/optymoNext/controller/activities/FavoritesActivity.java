package com.therolf.optymoNext.controller.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.FavoritesController;
import com.therolf.optymoNext.controller.OptymoNetworkController;
import com.therolf.optymoNext.vue.adapters.OptymoLineAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoStopAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoStop;

public class FavoritesActivity extends TopViewActivity {

    private Spinner stopSpinner;

    private OptymoLine[] lines = null;
    private OptymoStop[] stopsOfTheLine = null;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_add_favorite);

        lines = OptymoNetworkController.getInstance().getLines();

        final Spinner lineSpinner = findViewById(R.id.add_line_spinner);
        stopSpinner = findViewById(R.id.add_direction_spinner);
        Button addButton = findViewById(R.id.add_button);

        lineSpinner.setAdapter(new OptymoLineAdapter(this, lines));

        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                stopsOfTheLine = lines[position].getStops();
                stopSpinner.setAdapter(new OptymoStopAdapter(FavoritesActivity.this, stopsOfTheLine));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        addButton.setOnClickListener(view -> {
            if (stopsOfTheLine != null && stopsOfTheLine.length != 0 && stopSpinner.getSelectedItemPosition() < stopsOfTheLine.length) {
                OptymoDirection newDirection = new OptymoDirection(
                        lines[lineSpinner.getSelectedItemPosition()].getNumber(),
                        lines[lineSpinner.getSelectedItemPosition()].getName(),
                        stopsOfTheLine[stopSpinner.getSelectedItemPosition()].getName(),
                        stopsOfTheLine[stopSpinner.getSelectedItemPosition()].getSlug()
                );

                FavoritesController.getInstance().addFavorite(newDirection, FavoritesActivity.this);
                for(OptymoDirection dir : FavoritesController.getInstance().getFavorites()) {
                    System.out.println(dir);
                }

                Toast.makeText(FavoritesActivity.this, getResources().getString(R.string.add_fav_toast_nb_of_fav, FavoritesController.getInstance().size()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        MainActivity.destroyIntent();
        super.onDestroy();
    }
}

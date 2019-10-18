package com.therolf.optymoNext.controller.activities;

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
import com.therolf.optymoNext.vue.adapters.OptymoDirectionAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoLineAdapter;
import com.therolf.optymoNext.vue.adapters.OptymoStopAdapter;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoStop;

import java.util.Arrays;

public class FavoritesActivity extends AppCompatActivity {

    private Spinner stopSpinner;

    private OptymoLine[] lines = null;
    private OptymoStop[] stopsOfTheLine = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favorite);

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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stopsOfTheLine != null && stopsOfTheLine.length != 0 && stopSpinner.getSelectedItemPosition() < stopsOfTheLine.length) {
                    OptymoFavoritesController.getInstance(FavoritesActivity.this).addElement(
                            new OptymoDirection(
                                    lines[lineSpinner.getSelectedItemPosition()].getNumber(),
                                    lines[lineSpinner.getSelectedItemPosition()].getName(),
                                    stopsOfTheLine[stopSpinner.getSelectedItemPosition()].getName(),
                                    stopsOfTheLine[stopSpinner.getSelectedItemPosition()].getSlug()
                            )
                    );
                    Toast.makeText(FavoritesActivity.this, "You have now " + OptymoFavoritesController.getInstance(FavoritesActivity.this).size() + " favorites!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        MainActivity.destroyIntent();
        super.onDestroy();
    }

    void setAdapter(OptymoDirection[] directions) {
        System.err.println(Arrays.toString(directions));
        if(directions != null)
            stopSpinner.setAdapter(new OptymoDirectionAdapter(this, directions));
    }
}

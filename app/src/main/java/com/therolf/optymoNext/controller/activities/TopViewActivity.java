package com.therolf.optymoNext.controller.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;

@SuppressWarnings({"unused", "SameParameterValue"})
public abstract class TopViewActivity extends AppCompatActivity {

    protected ImageView topSearchButton;
    protected ImageView topCloseButton;

    protected void onCreate(@Nullable Bundle savedInstanceState, int viewID) {
        onCreate(savedInstanceState, viewID, false);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState, int viewID, boolean enableSearchIcon) {
        super.onCreate(savedInstanceState);
        setContentView(viewID);

        topSearchButton = findViewById(R.id.top_search_icon);
        topCloseButton = findViewById(R.id.top_close_icon);

        if(enableSearchIcon) {
            if(topSearchButton != null) topSearchButton.setVisibility(View.VISIBLE);
            if(topCloseButton != null) topCloseButton.setVisibility(View.GONE);
        } else {
            if(topSearchButton != null) topSearchButton.setVisibility(View.GONE);
            if(topCloseButton != null) topCloseButton.setVisibility(View.VISIBLE);
        }

        if(!enableSearchIcon && topCloseButton != null) {
            topCloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }
}

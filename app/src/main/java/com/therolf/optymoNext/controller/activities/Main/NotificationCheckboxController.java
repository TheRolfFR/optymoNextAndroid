package com.therolf.optymoNext.controller.activities.Main;

import android.content.Intent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.notifications.NotificationService;

@SuppressWarnings("unused")
class NotificationCheckboxController {

    private CheckBox checkBox;

    NotificationCheckboxController(AppCompatActivity context) {

        TextView label = context.findViewById(R.id.main_authorize_notification_text);
        this.checkBox = context.findViewById(R.id.main_authorize_notification_checkbox);

        // add click listener checkbox
        this.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent refreshIntent = new Intent(context.getApplicationContext(), NotificationService.class);
            if(isChecked) {
                refreshIntent.setAction(NotificationService.REFRESH_ACTION);
            } else {
                refreshIntent.setAction(NotificationService.CANCEL_ACTION);
            }

            Log.d("OptymoNext", "" + checkBox.isChecked());

            context.startService(refreshIntent);
        });

        // add click trugger with label
        label.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
    }
}

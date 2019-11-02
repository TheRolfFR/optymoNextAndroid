package com.therolf.optymoNext.controller.notifications;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class OnBoot extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, NotificationService.class);
        context.startService(startServiceIntent);


        Toast.makeText(context, "coucou favorites", Toast.LENGTH_SHORT).show();

    }
}

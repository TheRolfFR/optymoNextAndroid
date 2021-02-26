package com.therolf.optymoNext.controller.notifications;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@SuppressWarnings("unused")
public class OnBoot extends BroadcastReceiver {

    private static int index;
    private static NotificationController nCon;

    public static int getIndex() {
        return index;
    }

    public static void setIndex(int index) {
        OnBoot.index = index;
    }

    public static void increaseIndex(int value) {
        OnBoot.index += value;
    }

    public static NotificationController getNotificationController() {
        if(nCon == null) {
            nCon = new NotificationController();
        }

        return nCon;
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, NotificationService.class);
//        Log.d("optymo", "" + index);
        if(intent.getAction() != null) {
            startServiceIntent.setAction(intent.getAction());
        }
        context.startService(startServiceIntent);
    }
}

package com.therolf.optymoNext.controller.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.activities.SplashScreenActivity;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class NotificationController {

    private static final String NOTIFICATION_DISPLAY_KEY = "display_notification_key";
    private static final boolean NOTIFICATION_DISPLAY_DEFAULT = false;

    private static final String NEXT_SIX_OFFSET_KEY = "offset";

    private RemoteViews notificationLayoutExpanded;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private int numberOfLines = 0;

    private int[] layoutIds = {
            R.id.notification_next_stop_0,
            R.id.notification_next_stop_1,
            R.id.notification_next_stop_2,
            R.id.notification_next_stop_3,
            R.id.notification_next_stop_4,
            R.id.notification_next_stop_5
    };

    private int[] bgIds = {
            R.id.notification_next_stop_line_bg_0,
            R.id.notification_next_stop_line_bg_1,
            R.id.notification_next_stop_line_bg_2,
            R.id.notification_next_stop_line_bg_3,
            R.id.notification_next_stop_line_bg_4,
            R.id.notification_next_stop_line_bg_5
    };

    private int[] textIds = {
            R.id.notification_next_stop_main_0,
            R.id.notification_next_stop_main_1,
            R.id.notification_next_stop_main_2,
            R.id.notification_next_stop_main_3,
            R.id.notification_next_stop_main_4,
            R.id.notification_next_stop_main_5
    };

    private int[] colors;

    private int[] nextTimesIds = {
            R.id.notification_next_stop_time_0,
            R.id.notification_next_stop_time_1,
            R.id.notification_next_stop_time_2,
            R.id.notification_next_stop_time_3,
            R.id.notification_next_stop_time_4,
            R.id.notification_next_stop_time_5
    };

    private int[] fbgIds = {
            R.id.notification_expanded_fbg_0,
            R.id.notification_expanded_fbg_1,
            R.id.notification_expanded_fbg_2,
            R.id.notification_expanded_fbg_3,
            R.id.notification_expanded_fbg_4,
            R.id.notification_expanded_fbg_5
    };

    private int[] numberIds = {
            R.id.notification_next_stop_number_0,
            R.id.notification_next_stop_number_1,
            R.id.notification_next_stop_number_2,
            R.id.notification_next_stop_number_3,
            R.id.notification_next_stop_number_4,
            R.id.notification_next_stop_number_5,
    };

    private StringBuilder stringBuilder = new StringBuilder();
    private DateFormat dateFormat;
    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();

    private String title = "";

    private Intent resultIntent;
    private PendingIntent pIntent;

    private Intent refreshIntent;
    private PendingIntent refreshPendingIntent;

    private Intent nextSixIntent;
    private PendingIntent nextSixPendingIntent;

    private static final int NOTIFICATION_ID = 234;
    private static final String NOTIFICATION_CHANNEL_ID = "optymo_next_times_01";
    private static final CharSequence NOTIFICATION_CHANNEL_NAME = "optymo_next_times";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Channel only used to show persistent next times notification";

    public static boolean isNotificationShown(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        return preferences.getBoolean(NOTIFICATION_DISPLAY_KEY, NOTIFICATION_DISPLAY_DEFAULT);
    }

    @SuppressLint("ApplySharedPref")
    public static void setNotificationShown(Context context, boolean state) {
        SharedPreferences preferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        preferences.edit().putBoolean(NOTIFICATION_DISPLAY_KEY, state).commit();
    }

    void run(Context context) {
        if(builder != null || !isNotificationShown(context)) {
            return;
        }

        // set up colors
        colors = new int[10];
        colors[0] = context.getResources().getColor(R.color.colorLineDefault);
        int[] numbers = {1, 2, 3, 4, 5, 8, 9};
        for (int number : numbers) {
            int id = context.getResources().getIdentifier("colorLine" + number, "color", context.getPackageName());
            colors[number] = context.getResources().getColor(id);
        }

        notificationLayoutExpanded = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);

        // hide all views
        for (int layoutId : layoutIds) {
            notificationLayoutExpanded.setViewVisibility(layoutId, View.GONE);
        }

        // date format
        dateFormat = new SimpleDateFormat("HH:mm", context.getResources().getConfiguration().locale);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // channel for greater versions than 8.0
        NotificationChannel mChannel = null; // = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // cannot be stored as constant because require api 24 min
            int importance = NotificationManager.IMPORTANCE_MIN;
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            mChannel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            mChannel.setShowBadge(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        resultIntent = new Intent(context.getApplicationContext(), SplashScreenActivity.class);
        pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), resultIntent, 0);

        refreshIntent = new Intent(context.getApplicationContext(), OnBoot.class);
        refreshIntent.setAction(NotificationService.REFRESH_ACTION);
        refreshPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis()+1, refreshIntent, 0);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notification_expanded_refresh, refreshPendingIntent);

        nextSixIntent = new Intent(context.getApplicationContext(), OnBoot.class);
        nextSixIntent.putExtra(NEXT_SIX_OFFSET_KEY, 0);
        nextSixIntent.setAction(NotificationService.NEXT_SIX_GET);
        nextSixPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), nextSixIntent, 0);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.notification_expanded_next, nextSixPendingIntent);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SplashScreenActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_form)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setCustomBigContentView(notificationLayoutExpanded)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //to show content in lock screen
                .setContentTitle(context.getString(R.string.update_never))
                .setContentIntent(resultPendingIntent)
                .setContentText(context.getString(R.string.notification_content_text));

        //noinspection ConstantConditions
        if (mChannel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(mChannel.getId());
        }

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    void resetNotificationBody() {
        // reset string builder
        stringBuilder.setLength(0);

        // hide all views
        if(notificationLayoutExpanded != null) {
            for (int layoutId : layoutIds) {
                notificationLayoutExpanded.setViewVisibility(layoutId, View.GONE);
            }
        }

        // reset number of lines
        numberOfLines = 0;
    }

    void appendToNotificationBody(OptymoNextTime nextTime) {
        if(notificationLayoutExpanded != null && numberOfLines < layoutIds.length) {
            // update bg color
//            notificationLayoutExpanded.setBa

            // update main text
            notificationLayoutExpanded.setTextViewText(numberIds[numberOfLines], "" + nextTime.getLineNumber());
            notificationLayoutExpanded.setTextViewText(textIds[numberOfLines], nextTime.directionToString().substring(4));
            notificationLayoutExpanded.setTextViewText(nextTimesIds[numberOfLines], nextTime.getNextTime());

            // show layout
            notificationLayoutExpanded.setViewVisibility(layoutIds[numberOfLines], View.VISIBLE);

            // total cheating to change a background
            notificationLayoutExpanded.setTextColor(bgIds[numberOfLines], colors[nextTime.getLineNumber()]);
            notificationLayoutExpanded.setInt(bgIds[numberOfLines], "setBackgroundColor", colors[nextTime.getLineNumber()]);
            notificationLayoutExpanded.setInt(fbgIds[numberOfLines], "setBackgroundColor", colors[nextTime.getLineNumber()]);

            // increase number of lines
            numberOfLines++;

            // will update notification title
            this.sendNotification();
        }
    }

    private void updateTitle(String title) {
        if(notificationLayoutExpanded == null)
            return;

        // update data title
        this.title = title;

        // update title textview
        notificationLayoutExpanded.setTextViewText(R.id.notification_expanded_title, this.title);

        // will update notification title
        this.sendNotification();
    }

    private void sendNotification() {
        if(builder != null) {
            builder = builder.setContentTitle(title);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void setNeverUpdatedTitle(Context context) {
        updateTitle(context.getString(R.string.update_never));
    }

    void setPendingTitle(Context context) {
        updateTitle(context.getString(R.string.update_pending));
    }

    void setNoFavoritesTitle(Context context) {
        updateTitle(context.getString(R.string.no_favorites));
    }

    void setUpdatedAtTitle(Context context) {
        if(dateFormat == null)
            return;

        Date date = new Date();
        String dateFormatted = dateFormat.format(date);
        updateTitle(context.getString(R.string.update_last, dateFormatted));
    }

    void cancelNotification(Context context) {
        if(notificationManager == null)
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notificationManager != null)
            notificationManager.cancel(NOTIFICATION_ID);
    }
}

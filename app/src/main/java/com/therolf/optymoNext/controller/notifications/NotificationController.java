package com.therolf.optymoNext.controller.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
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

    private static final String NEXT_SIX_OFFSET_KEY = "offset";

    private RemoteViews notificationLayoutExpanded;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    private StringBuilder stringBuilder = new StringBuilder();
    private DateFormat dateFormat;
    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();

    private String title = "";

    private Intent nextSixIntent;
    private PendingIntent nextSixPendingIntent;

    private static final int NOTIFICATION_ID = 234;
    private static final String NOTIFICATION_CHANNEL_ID = "optymo_next_times_01";
    private static final CharSequence NOTIFICATION_CHANNEL_NAME = "optymo_next_times";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Channel only used to show persistent next times notification";

    void run(Context context) {
        if(builder != null) {
            return;
        }

        notificationLayoutExpanded = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);

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

        Intent resultIntent = new Intent(context.getApplicationContext(), SplashScreenActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), resultIntent, 0);

        Intent refreshIntent = new Intent(context.getApplicationContext(), OnBoot.class);
        refreshIntent.setAction(NotificationService.REFRESH_ACTION);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), refreshIntent, 0);

        nextSixIntent = new Intent(context.getApplicationContext(), OnBoot.class);
        nextSixIntent.putExtra(NEXT_SIX_OFFSET_KEY, 0);
        nextSixIntent.setAction(NotificationService.NEXT_SIX_GET);
        nextSixPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), nextSixIntent, 0);

        builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_form)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.update_never))
                .setContentText(context.getString(R.string.notification_content_text))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(notificationLayoutExpanded)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //to show content in lock screen
                .addAction(R.drawable.ic_refresh, context.getString(R.string.notification_refresh_button_text), refreshPendingIntent)
                .addAction(R.drawable.ic_next, context.getString(R.string.notification_others_button_text), nextSixPendingIntent);

        //noinspection ConstantConditions
        if (mChannel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(mChannel.getId());
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SplashScreenActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    void resetNotificationBody() {
        // reset string builder
        stringBuilder.setLength(0);
        // reset string in notification also
        notificationLayoutExpanded.setTextViewText(R.id.notification_expanded_content, stringBuilder);

//        this.sendNotification();
    }

    void appendToNotificationBody(String line) {
        // add new end line
        if(stringBuilder.length() > 0)
            stringBuilder.append('\n');

        // add the line to the buffer
        stringBuilder.append(line);

        // reset string in notification also
        notificationLayoutExpanded.setTextViewText(R.id.notification_expanded_content, stringBuilder);

//        this.sendNotification();
    }

    private void updateTitle(String title) {
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
        updateTitle(context.getString(R.string.update_pending));
    }

    void setTitlePending(Context context) {
        updateTitle(context.getString(R.string.update_pending));
    }

    void setUpdatedAtTitle(Context context) {
        Date date = new Date();
        String dateFormatted = dateFormat.format(date);
        updateTitle(context.getString(R.string.update_last, dateFormatted));
    }
}

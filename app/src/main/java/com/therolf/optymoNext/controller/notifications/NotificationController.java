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
import com.therolf.optymoNext.vue.adapters.NextTimeItemAdapter;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class NotificationController {

    public static final String NEXT_SIX_OFFSET_KEY = "offset";

    private RemoteViews notificationLayoutExpanded;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    private StringBuilder buffer = new StringBuilder();
    private DateFormat dateFormat;
    private NextTimeItemAdapter nextTimeItemAdapter;
    private ArrayList<OptymoNextTime> nextTimes = new ArrayList<>();

    private String title = "";
    private NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

    private Intent nextSixIntent;
    private PendingIntent nextSixPendingIntent;

    private static final int NOTIFICATION_ID = 234;
    private static final String NOTIFICATION_CHANNEL_ID = "my_channel_01";
    private static final CharSequence NOTIFICATION_CHANNEL_NAME = "my_channel";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "This is my channel";

    public void run(Context context) {
        if(builder != null) {
            return;
        }

        nextTimeItemAdapter = new NextTimeItemAdapter(context, nextTimes);
        notificationLayoutExpanded = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);

        // date format
        dateFormat = new SimpleDateFormat("HH:mm", context.getResources().getConfiguration().locale);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

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
                .setStyle(inboxStyle)
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

    public void updateBody(ArrayList<OptymoNextTime> otherNextTimes) {
        if(inboxStyle == null) {
            inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(title);
        }

        for(OptymoNextTime n : otherNextTimes) {
            appendToNotificationBody(n.toString());
        }
    }

    public void resetNotificationBody() {
        inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);

        buffer.setLength(0);

        this.sendNotification();
    }

    public void appendToNotificationBody(String line) {
        if(inboxStyle == null) {
            inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(title);
        }
        inboxStyle.addLine(line);

        this.sendNotification();
    }

    public void updateTitle(String title) {
        if(inboxStyle == null) {
            inboxStyle = new NotificationCompat.InboxStyle();
        }

        this.title = title;

        inboxStyle.setBigContentTitle(title);

        this.sendNotification();
    }

    private void sendNotification() {
        if(builder != null) {
            builder
                    .setStyle(inboxStyle)
                    .setContentTitle(title);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public void setNeverUpdatedTitle(Context context) {
        updateTitle(context.getString(R.string.update_pending));
    }

    public void setTitlePending(Context context) {
        updateTitle(context.getString(R.string.update_pending));
    }

    public void setUpdatedAtTitle(Context context) {
        Date date = new Date();
        String dateFormatted = dateFormat.format(date);
        updateTitle(context.getString(R.string.update_last, dateFormatted));
    }
}

package com.therolf.optymoNext.controller.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.activities.SplashScreenActivity;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings({"unused", "WeakerAccess"})
public class NotificationController {

    private static NotificationManager notificationManager;
    @SuppressLint("StaticFieldLeak")
    private static NotificationCompat.Builder builder;

    private static StringBuilder buffer = new StringBuilder();
    private static DateFormat dateFormat;

    private static final int NOTIFICATION_ID = 234;

    public static void run(Context context) {
        if(builder != null) {
            return;
        }

        // date format
        dateFormat = new SimpleDateFormat("HH:mm", context.getResources().getConfiguration().locale);

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "my_channel_01";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.setShowBadge(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        Intent resultIntent = new Intent(context.getApplicationContext(), SplashScreenActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), resultIntent, 0);

        Intent refreshIntent = new Intent(context.getApplicationContext(), OnBoot.class);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), refreshIntent, 0);

        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_form)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.update_never))
                .setContentText(context.getString(R.string.notification_content_text))
                .setOngoing(true)
                .addAction(R.drawable.ic_refresh, context.getString(R.string.notification_refresh_button_text), refreshPendingIntent);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SplashScreenActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static void updateBody(ArrayList<OptymoNextTime> nextTimes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < nextTimes.size(); i++) {
            OptymoNextTime nextTime = nextTimes.get(i);
            buffer.append(nextTime.toString());
            if(i < nextTimes.size() - 1)
                buffer.append('\n');
        }

        builder
                .setStyle(new NotificationCompat.BigTextStyle().bigText(buffer.toString()));
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void resetNotificationBody() {
        buffer.setLength(0);
        if(builder != null) {
            builder
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(""));
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    public static void appendToNotificationBody(String line) {
        if(buffer.length() != 0) {
            buffer.append('\n');
        }

        buffer.append(line);

        builder
                .setStyle(new NotificationCompat.BigTextStyle().bigText(buffer.toString()));
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void updateTitle(String title) {
        builder.setContentTitle(title);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void setNeverUpdatedTitle(Context context) {
        updateTitle(context.getString(R.string.update_pending));
    }

    public static void setUpdatedAtTitle(Context context) {
        Date date = new Date();
        String dateFormatted = dateFormat.format(date);
        updateTitle(context.getString(R.string.update_last, dateFormatted));
    }
}

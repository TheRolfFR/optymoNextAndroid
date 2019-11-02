package com.therolf.optymoNext.controller.notifications;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.therolf.optymoNext.controller.FavoritesController;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.util.ArrayList;

public class NotificationService extends IntentService {

    private int numberOfRequests;
    private ArrayList<NotificationService.OptymoGetNextTime> lastRequests = new ArrayList<>();

    public NotificationService() {
        super("MyNotificationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // get favorites
        OptymoDirection[] favorites = FavoritesController.getInstance().readFile(this).getFavorites();

//        Toast.makeText(this, favorites.length + " favorites", Toast.LENGTH_SHORT).show();

        // cancel all remaining requests
        while(lastRequests.size() > 0) {
            NotificationService.OptymoGetNextTime request = lastRequests.get(0);
            request.cancel(true);
            lastRequests.remove(0);
        }

        // empty notification body
        NotificationController.resetNotificationBody();
        // change title to pending
        NotificationController.setNeverUpdatedTitle(this);

        // reset number of requests
        numberOfRequests = 0;


        // start for each direction a request
        NotificationService.OptymoGetNextTime tmp;
        for(OptymoDirection favorite : favorites) {
            tmp = new NotificationService.OptymoGetNextTime();
            lastRequests.add(tmp);
            tmp.execute(favorite);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class OptymoGetNextTime extends AsyncTask<OptymoDirection, Void, OptymoNextTime> {

        @Override
        protected OptymoNextTime doInBackground(OptymoDirection... optymoDirections) {
            OptymoNextTime result = null;
            OptymoNextTime latestResult = null;

            OptymoNextTime[] nextTimes = OptymoNetwork.getNextTimes(optymoDirections[0].getStopSlug());
            int i = 0;
            while (i < nextTimes.length && result == null) {
                if (optymoDirections[0].toString().equals(nextTimes[i].directionToString()))
                    result = nextTimes[i];
                if (latestResult == null) {
                    latestResult = nextTimes[i];
                } else if (latestResult.compareTo(nextTimes[i]) < 0)
                    latestResult = nextTimes[i];
                ++i;
            }

            // provide default result > last time got
            if (result == null) {
                String nullResult = OptymoNextTime.NULL_TIME_VALUE;
                if (latestResult != null)
                    nullResult = "> " + latestResult.getNextTime();
                result = new OptymoNextTime(optymoDirections[0].getLineNumber(), optymoDirections[0].getDirection(), optymoDirections[0].getStopName(), optymoDirections[0].getStopSlug(), nullResult);
            }

            return result;
        }

        @Override
        protected void onPostExecute(OptymoNextTime nextTime) {
            super.onPostExecute(nextTime);

            numberOfRequests++;
            if (numberOfRequests >= lastRequests.size())
                NotificationController.setUpdatedAtTitle(NotificationService.this);

            // update notification
            NotificationController.appendToNotificationBody(nextTime.toString());
        }

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        return;
    }
}

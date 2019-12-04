package com.therolf.optymoNext.controller.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.therolf.optymoNext.controller.global.GlobalApplication;
import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class NotificationService extends IntentService {

    public static final String REFRESH_ACTION = "refresh_action";
    public static final String NEXT_SIX_GET = "next_six_get";
    public static final String CANCEL_ACTION = "notification_cancel_all";
    private NotificationController notificationController = new NotificationController();

    private int numberOfRequests;
    private ArrayList<NextTimeRequest> lastRequests = new ArrayList<>();

    public NotificationService() {
        super("MyNotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent workIntent) {
        if(workIntent != null && workIntent.getAction() != null) {
            if(workIntent.getAction().equals(REFRESH_ACTION) || workIntent.getAction().equals(NEXT_SIX_GET) || workIntent.getAction().equals(CANCEL_ACTION)) {
                handleAction(workIntent.getAction());
            }
        }
    }

    private void handleAction(String action) {
        //  get the favorites
        OptymoDirection[] fav = ((GlobalApplication) getApplication()).getFavoritesController().getFavorites();
//        Log.d("optymo", "" + fav.length);

        // if action is cancel we cancel
        if(action.equals(CANCEL_ACTION)) {
            notificationController.cancelAll(this);
            return;
        }

        // reset index if refresh
        if(action.equals(REFRESH_ACTION))
            OnBoot.setIndex(0);
        else { // else its next
            // increase index of 6
            OnBoot.increaseIndex(6);
//            Log.d("optymo", "" + OnBoot.getIndex());

            // but reset if greater than favorites length
            if(OnBoot.getIndex() >= fav.length) {
                OnBoot.setIndex(0);
            }
//            Log.d("optymo", "" + OnBoot.getIndex());
        }

        // reset requests
        numberOfRequests = 0;

        // run and reset notification body and title
        notificationController.run(this);
        notificationController.resetNotificationBody();

        // set appropriate title
        if(fav.length == 0) {
            notificationController.setNoFavoritesTitle(this);
        } else {
            notificationController.setPendingTitle(this);
        }

        // clear requests
        while(lastRequests.size() > 0) {
            NextTimeRequest request = lastRequests.get(0);
            request.cancel(true);
            lastRequests.clear();
        }

        // make maximum 6 requests
        int rNumber = 0;
        int startIndex = OnBoot.getIndex();
        while (startIndex + rNumber < fav.length && rNumber < 6) {
            Log.d("optymonext", "request #" + rNumber);

            // make a new request
            NextTimeRequest request = new NextTimeRequest(this);
            request.execute(fav[startIndex + rNumber]);
            lastRequests.add(request);

            ++rNumber;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    private static class NextTimeRequest extends AsyncTask<OptymoDirection, Void, OptymoNextTime> {

        private WeakReference<NotificationService> reference;

        NextTimeRequest(NotificationService service) {
            this.reference = new WeakReference<>(service);
        }

        @Override
        protected OptymoNextTime doInBackground(OptymoDirection... optymoDirections) {
            OptymoNextTime result = null;
            OptymoNextTime latestResult = null;

            OptymoNextTime[] nextTimes = new OptymoNextTime[0];
            try {
                nextTimes = OptymoNetwork.getNextTimes(optymoDirections[0].getStopSlug());
            } catch (IOException e) {
                e.printStackTrace();
            }
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

            // get activity
            NotificationService service = reference.get();
            if (service == null) return;

            service.numberOfRequests++;
            if (service.numberOfRequests >= service.lastRequests.size())
                service.notificationController.setUpdatedAtTitle(service);

            // add line to notification if first 6
            if(service.numberOfRequests < 7)
                service.notificationController.appendToNotificationBody(nextTime);
        }

    }
}

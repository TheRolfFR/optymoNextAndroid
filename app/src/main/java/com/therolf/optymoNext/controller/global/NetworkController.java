package com.therolf.optymoNext.controller.global;

import android.content.Context;
import android.util.Log;

import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoStop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@SuppressWarnings({"unused"})
public class NetworkController {

    private static final String TAG = "NetworkGen";

    private OptymoNetwork network;

    NetworkController() {
        network = new OptymoNetwork();
    }

    public boolean isGenerated() {
        return network.isGenerated();
    }

    @SuppressWarnings("SameParameterValue")
    void generate(Context context) {
        // iniialize default values
        String stopsJsonString = "";

        // try to parse JSON
        try {
            InputStream jsonInputStream = context.getResources().openRawResource(context.getResources().getIdentifier("network", "raw", context.getPackageName()));
            byte[] buffer = new byte[jsonInputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            jsonInputStream.read(buffer);

            // affect json string
            stopsJsonString = new String(buffer);

        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        if(!stopsJsonString.equals("")) {
            network.begin(stopsJsonString);
        } else {
            Log.e("OptymoNext", "[NetworkController] failed to read json file");
            System.exit(-1);
        }
    }

    public OptymoStop[] getStops() {
        return network.getStops();
    }
    public OptymoLine[] getLines() {
        return network.getLines();
    }

    public OptymoStop getStopBySlug(String key) {
        return network.getStopBySlug(key);
    }
    public OptymoLine getLineByNumberAndName(int number, String name) { return network.getLineByNumberAndName(number, name); }

    @SuppressWarnings("WeakerAccess")
    public void addProgressListener(OptymoNetwork.ProgressListener listener) {
        network.addNetworkGenerationListener(listener);
    }

    public void addProgressListenerIfNotGenenerated(OptymoNetwork.ProgressListener listener) {
        if(!network.isGenerated()) {
            addProgressListener(listener);
        } else {
            listener.OnGenerationEnd(true);
        }
    }

    public String getResultJSON() {
        return network.getResultJson();
    }
}

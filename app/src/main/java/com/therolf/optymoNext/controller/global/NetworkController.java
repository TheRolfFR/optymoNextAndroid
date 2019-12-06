package com.therolf.optymoNext.controller.global;

import android.content.Context;
import android.util.Log;

import com.therolf.optymoNextModel.OptymoLine;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoStop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@SuppressWarnings({"unused"})
public class NetworkController {

    private static final String TAG = "NetworkGen";

    private OptymoNetwork network;
    private String jsonFileName;

    NetworkController() {
        this.jsonFileName = "network.json";
        network = new OptymoNetwork();
    }

    public boolean isGenerated() {
        return network.isGenerated();
    }

    void generate(Context context) {
        generate(context, false);
    }

    @SuppressWarnings("SameParameterValue")
    private void generate(Context context, boolean forceXml) {
        // iniialize default values
        InputStream xmlInputStream;
        String stopsJsonString = "";
        boolean jsonParsingWentWell = false;

        // try to parse JSON
        try {
            FileInputStream fis = context.openFileInput(jsonFileName);
            byte[] buffer = new byte[fis.available()];
            //noinspection ResultOfMethodCallIgnored
            fis.read(buffer);
            stopsJsonString = new String(buffer);
            jsonParsingWentWell = true;
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

        if(!jsonParsingWentWell)
            forceXml = true;

        // parse XML
        xmlInputStream = (context.getResources().openRawResource(context.getResources().getIdentifier("belfort", "raw", context.getPackageName())));

        //noinspection ConstantConditions
        if(forceXml || jsonParsingWentWell) {
            network.begin(stopsJsonString, xmlInputStream, forceXml);
        } else {
            Log.e(TAG, "Failed to parse XML or JSON: " + jsonParsingWentWell);
        }

        // save json resulting if xml was generated
        String result = network.getResultJson();
        if(!result.equals("")) {
            try {
                FileOutputStream fos = context.openFileOutput(jsonFileName, Context.MODE_PRIVATE);
                fos.write(network.getResultJson().getBytes());
                fos.close();
//                Log.d("stops", network.getResultJson());
            } catch (java.io.IOException e) {
//                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
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

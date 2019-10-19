package com.therolf.optymoNext.controller;

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

@SuppressWarnings({"unused", "WeakerAccess"})
public class OptymoNetworkController {

    private static final String TAG = "NetworkGen";

    private OptymoNetwork network;
    private String jsonFileName;

    private static OptymoNetworkController networkController = new OptymoNetworkController();

    public static OptymoNetworkController getInstance() {
        return networkController;
    }

    private OptymoNetworkController() {
        this.jsonFileName = "stops.json";
        network = new OptymoNetwork();
    }

    public boolean isGenerated() {
        return network.isGenerated();
    }

    public void generate(Context context) {
        generate(context, false);
    }

    public void generate(Context context, boolean forceXml) {
        // iniialize default values
        InputStream xmlInputStream;
        String stopsJsonString = "";
        boolean jsonParsingWentWell = false;

        // try to parse JSON
        try {
            FileInputStream fis = context.openFileInput(jsonFileName);
            System.out.println();
            byte[] buffer = new byte[fis.available()];
            //noinspection ResultOfMethodCallIgnored
            fis.read(buffer);
            stopsJsonString = new String(buffer);
            jsonParsingWentWell = true;
        } catch (IOException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

//        Log.i("OptymoNetworkController", "jsonParsingWentWell : " + jsonParsingWentWell);
//        Log.i("OptymoNetworkController", stopsJsonString);

        // parse XML
        xmlInputStream = (context.getResources().openRawResource(context.getResources().getIdentifier("belfort", "raw", context.getPackageName())));

        if(forceXml || jsonParsingWentWell) {
            network.begin(stopsJsonString, xmlInputStream, forceXml);
        } else {
            Log.e(TAG, "Failed to parse XML or JSON: " + jsonParsingWentWell);
        }

        // save json resulting if xml was generated
        String result = network.getResultJson();
        System.out.println(result);
        if(!result.equals("")) {
            try {
                FileOutputStream fos = context.openFileOutput(jsonFileName, Context.MODE_PRIVATE);
                fos.write(network.getResultJson().getBytes());
                fos.close();
                Log.d("stops", network.getResultJson());
            } catch (java.io.IOException e) {
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    }

    public OptymoStop[] getStops() {
        return network.getStops();
    }

    public OptymoLine[] getLines() {
        return network.getLines();
    }

    public OptymoStop getStopByKey(String key) {
        return network.getStopBySlug(key);
    }

    public void setProgressListener(OptymoNetwork.ProgressListener listener) {
        network.setNetworkGenerationListener(listener);
    }

    public String getResultJSON() {
        return network.getResultJson();
    }
}

package com.therolf.optymoNext.controller;

import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNextModel.OptymoDirection;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

@SuppressWarnings({"unused", "UnusedReturnValue", "WeakerAccess"})
public class OptymoFavoritesController {

    private final static String STOP_KEY_KEY = "stopKey";
    private final static String STOP_NAME_KEY = "stopName";
    private final static String LINE_NUMBER_KEY = "lineNumber";
    private final static String DIRECTION_KEY = "direction";

    private ArraySharedPreferences preferences;

    private static OptymoFavoritesController favoritesController = null;

    public static OptymoFavoritesController getInstance(AppCompatActivity context) {
        if(favoritesController == null)
            favoritesController = new OptymoFavoritesController(context);
        return favoritesController;
    }

    private OptymoFavoritesController(AppCompatActivity context) {
        preferences = new ArraySharedPreferences(context, "favorites");
    }

    public int size() {
        return preferences.size();
    }

    public OptymoDirection[] getFavorites() {
        int size = this.size();

        // potential result
        OptymoDirection[] result = new OptymoDirection[size];
        OptymoDirection tmp = null;

        // loop and add while no tmp element is null or the size done
        int i = 0;
        if(size > 0) {
            do {
                tmp = getElementAt(i);
                if(tmp != null)
                    result[i] = tmp;

                ++i;
            } while (tmp != null && i < size);
        }

        // if tmp was never null return result
        if(size == 0 || tmp != null)
            return result;

        // else return empty array
        return new OptymoDirection[0];
    }

    public int removeFavoriteAt(int index) {
        return preferences.removeElementAt(index);
    }

    public int addElement(OptymoDirection direction) {
        try {
            JSONStringer stringer = new JSONStringer().object()
                .key(STOP_KEY_KEY).value(direction.getStopSlug())
                .key(STOP_NAME_KEY).value(direction.getStopName())
                .key(LINE_NUMBER_KEY).value(direction.getLineNumber())
                .key(DIRECTION_KEY).value(direction.getDirection())
            .endObject();

            preferences.addElement(stringer.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return preferences.size();
    }

    public OptymoDirection getElementAt(int index) {
        String el = preferences.getElement(index, "");
        OptymoDirection result = null;

        try {
            JSONObject object = new JSONObject(el);
            result = new OptymoDirection(object.getInt(LINE_NUMBER_KEY), object.getString(DIRECTION_KEY), object.getString(STOP_NAME_KEY), object.getString(STOP_KEY_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void removeAllElements() {
        preferences.removeAllElements();
    }
}

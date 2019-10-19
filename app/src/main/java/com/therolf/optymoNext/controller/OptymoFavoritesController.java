package com.therolf.optymoNext.controller;

import android.util.ArrayMap;

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
    private ArrayMap<String, OptymoDirection> directionsList;

    private static OptymoFavoritesController favoritesController = null;

    public static OptymoFavoritesController getInstance(AppCompatActivity context) {
        if(favoritesController == null)
            favoritesController = new OptymoFavoritesController(context);
        return favoritesController;
    }

    private OptymoFavoritesController(AppCompatActivity context) {
        preferences = new ArraySharedPreferences(context, "favorites");
        loadFavorites();
        System.out.println("Loaded " + size() + " favorites");
    }

    public int size() {
        return directionsList.size();
    }

    private void loadFavorites() {
        int size = preferences.size();

        // potential result
        ArrayMap<String, OptymoDirection> map = new ArrayMap<>();
        OptymoDirection tmp = null;

        // loop and add while no tmp element is null or the size done
        int i = 0;
        if(size > 0) {
            tmp = getElementAt(i);
            do {
                if(tmp != null && !map.containsKey(tmp.toString()))
                    map.put(tmp.toString(), tmp);

                ++i;
                tmp = getElementAt(i);
            } while (tmp != null && i < size);
        }

        // if tmp was never null result is correct
        if(size == 0 || tmp != null)
            directionsList = map;
        else
            directionsList = new ArrayMap<>();
    }

    public OptymoDirection[] getFavorites() {
        return directionsList.values().toArray(new OptymoDirection[0]);
    }

    public int removeFavoriteAt(int index) {
        return preferences.removeElementAt(index);
    }

    public int addElement(OptymoDirection direction) {
        if(!directionsList.containsKey(direction.toString()))
            directionsList.put(direction.toString(), direction);

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

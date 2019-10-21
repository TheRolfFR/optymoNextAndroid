package com.therolf.optymoNext.controller;

import android.content.Context;
import android.util.ArrayMap;

import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.io.FileInputStream;
import java.io.FileOutputStream;

@SuppressWarnings({"WeakerAccess"})
public class FavoritesController {

    private static final String PATH = "favorites.txt";
    private static final char LINE_SEPARATOR = '!';
    private static final char FIELD_SEPARATOR = '|';
    private static FavoritesController controller = null;

    @SuppressWarnings("unused")
    public static FavoritesController getInstance(Context context) {
        if(controller == null)
            controller = new FavoritesController(context);
        return controller;
    }

    private ArrayMap<String, OptymoDirection> favorites = new ArrayMap<>();
    private boolean isFileRead = false;
    private OptymoNetwork.ProgressListener progressListener;

    @SuppressWarnings("unused")
    public void setProgressListener(OptymoNetwork.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    private FavoritesController(Context context) {
        readFile(context);
    }

    private static String escapeQuotes(String str) {
        if(str != null && str.length() > 0) {
            return str.replaceAll("[\\W]", "\\\\$0"); // \W designates non-word characters
        }
        return "";
    }

    public void readFile(Context context) {
        if(!isFileRead) {
            try {
                FileInputStream fis = context.openFileInput(PATH);

                byte[] arr = new byte[fis.available()];
                //noinspection ResultOfMethodCallIgnored
                fis.read(arr);

                String result = new String(arr);
                String[] lines, parts;
                OptymoDirection tmp;

                lines = result.split("" + LINE_SEPARATOR);
                if(lines.length > 1) {
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.length() != 0) {
                            if(progressListener != null)
                                progressListener.OnProgressUpdate(i, Math.max(i, lines.length-1), "favorite");

                            parts = line.split(escapeQuotes("" + FIELD_SEPARATOR));
                            if (parts.length == 4) {
                                tmp = new OptymoDirection(
                                        Integer.parseInt(parts[0]),
                                        parts[1],
                                        parts[2],
                                        parts[3]);
                                favorites.put(tmp.toString(), tmp);
                            }
                        }
                    }
                    if(progressListener != null)
                        progressListener.OnGenerationEnd(true);
                }

            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            System.out.println("Loaded " + size() + " favorites!");
            isFileRead = true;
        }
    }

    public void writeFile(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(PATH, Context.MODE_PRIVATE);
            OptymoDirection tmp;

            for(int i = 0; i < favorites.size(); i++) {
                tmp = favorites.valueAt(i);
                if(tmp != null)
                    fos.write((tmp.getLineNumber() + "|" + tmp.getDirection() + "|" + tmp.getStopName() + "|" + tmp.getStopSlug() + "!").getBytes());
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void addFavorite(OptymoDirection direction, Context context) {
        if(!favorites.containsKey(direction.toString())) {
            favorites.put(direction.toString(), direction);
            writeFile(context);
        }
    }

    @SuppressWarnings("unused")
    public void removeAt(int index, Context context) {
        favorites.removeAt(index);
        writeFile(context);
    }

    @SuppressWarnings("unused")
    public void remove(OptymoNextTime nextTime, Context context) {
        favorites.remove(nextTime.directionToString());
        writeFile(context);
    }

    @SuppressWarnings("unused")
    public OptymoDirection[] getFavorites() {
        return favorites.values().toArray(new OptymoDirection[0]);
    }

    public int size() {
        return favorites.size();
    }
}

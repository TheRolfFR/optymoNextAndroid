package com.therolf.optymoNext.controller.global;

import android.content.Context;
import android.util.ArrayMap;

import com.therolf.optymoNextModel.OptymoDirection;
import com.therolf.optymoNextModel.OptymoNetwork.ProgressListener;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

@SuppressWarnings({"WeakerAccess", "unused"})
public class FavoritesController {

    private static final String PATH = "favorites.txt";
    private static final char LINE_SEPARATOR = '!';
    private static final String LINE_SEPARATOR_REGEX = "!";
    private static final char FIELD_SEPARATOR = '|';

    private ArrayMap<String, OptymoDirection> favorites = new ArrayMap<>();
    private boolean isFileRead = false;
    private ArrayList<ProgressListener> progressListener = new ArrayList<>();

    public void addProgressListener(ProgressListener progressListener) {
        this.progressListener.add(progressListener);
    }

    public void addProgressListenerIfNotLoaded(ProgressListener progressListener) {
        if(!isFileRead) {
            addProgressListener(progressListener);
        } else {
            progressListener.OnGenerationEnd(true);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String escapeQuotes(String str) {
        if(str != null && str.length() > 0) {
            return str.replaceAll("[\\W]", "\\\\$0"); // \W designates non-word characters
        }
        return "";
    }

    @SuppressWarnings({"UnusedReturnValue"})
    public FavoritesController readFile(Context context) {
        if(!isFileRead) {
            try {
                FileInputStream fis = context.openFileInput(PATH);

                byte[] arr = new byte[fis.available()];
                //noinspection ResultOfMethodCallIgnored
                fis.read(arr);

                String result = new String(arr);
                String[] lines, parts;
                OptymoDirection tmp;

//                System.err.println(result);

                lines = result.split(LINE_SEPARATOR_REGEX);
//                System.err.println(lines.length);
                if(result.indexOf(LINE_SEPARATOR) != -1) {
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
//                        System.err.println(line);
                        if (line.length() != 0) {
                            for(ProgressListener listener : progressListener)
                                listener.OnProgressUpdate(i, Math.max(i, lines.length-1), "favorite");

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
                    for(ProgressListener listener : progressListener)
                        listener.OnGenerationEnd(true);
                }

                fis.close();

            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            for(ProgressListener listener : progressListener)
                listener.OnGenerationEnd(true);
            isFileRead = true;

            return this;
        }

        for(ProgressListener listener : progressListener)
            listener.OnGenerationEnd(false);

        return this;
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

            fos.close();
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

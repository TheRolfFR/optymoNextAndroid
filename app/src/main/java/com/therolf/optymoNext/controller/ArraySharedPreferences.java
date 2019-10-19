package com.therolf.optymoNext.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue"})
public class ArraySharedPreferences {

    private static final String ARRAY_KEY = "Array.";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private String nameKeyPrefix;
    private String LENGTH_KEY;

    @SuppressLint("CommitPrefEdits")
    public ArraySharedPreferences(AppCompatActivity context, String name) {
        preferences = context.getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
        nameKeyPrefix = ARRAY_KEY + name + ".";
        LENGTH_KEY = nameKeyPrefix + "length";
    }

    public int size() {
        return preferences.getInt(LENGTH_KEY, 0);
    }

    public int addElement(String content) {
        System.out.println(content);
        int size = size();
        editor.putString(nameKeyPrefix + size, content).apply();
        editor.putInt(LENGTH_KEY, size + 1).apply();
        return size+1;
    }

    public String getElement(int index, String defaultValue) {
        return preferences.getString(nameKeyPrefix + index, defaultValue);
    }

    public int removeElementAt(int index) {
        int size = size();
        if(size == 0) {
            return 0;
        } else if(index < size) {
            // shift everything to the left
            for (int i = index; i < size-1; i++) {
                editor.putString(nameKeyPrefix + i, preferences.getString(nameKeyPrefix + (i+1), ""));
            }

            // returns the new size
            editor.putInt(LENGTH_KEY, size-1);
            return size-1;
        }

        // else wrong index
        return size;
    }

    public String[] getAllElements(String defaultValue) {
        String[] result = new String[this.size()];

        for(int i = 0; i < result.length; i++) {
            result[i] = getElement(i, defaultValue);
        }

        return result;
    }

    public void removeAllElements() {
        int size = this.size();
        for(int i = 0; i < size; i++) {
            this.removeElementAt(i);
        }

        editor.putInt(LENGTH_KEY, 0);
    }
}

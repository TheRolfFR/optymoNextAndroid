package com.therolf.optymoNextModel;

import java.text.Normalizer;
import java.util.Arrays;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OptymoLine implements Comparable<OptymoLine> {
    private int number;
    private String name;
    private OptymoStop[] stops;

    public OptymoLine(int number, String name) {
        this.number = number;
        this.name = name;
        this.stops = new OptymoStop[0];
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public OptymoStop[] getStops() {
        Arrays.sort(stops);
        return stops;
    }

    @SuppressWarnings("WeakerAccess")
    public void addStopToLine(OptymoStop stop) {
        for (OptymoStop optymoStop : stops) {
            if (optymoStop.equals(stop)) {
                return;
            }
        }

        // else add it to the list
        OptymoStop[] newTable = new OptymoStop[stops.length+1];
        System.arraycopy(stops, 0, newTable, 0, stops.length);
        newTable[stops.length] = stop;

        stops = newTable;
    }

    public String getCleanedToString() {
        return Normalizer.normalize(this.toString(), Normalizer.Form.NFD).replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "[" + this.number + "] " + this.name;
    }

    @Override
    public int compareTo(OptymoLine o) {
        if(this.number == o.number) {
            return this.name.compareTo(o.name);
        }

        return this.number < o.number ? -1 : 1;
    }
}

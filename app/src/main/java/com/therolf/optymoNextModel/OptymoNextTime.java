package com.therolf.optymoNextModel;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OptymoNextTime extends OptymoDirection {

    private String nextTime;

    public OptymoNextTime(int lineNumber, String direction, String stopName, String stopSlug, String nextTime) {
        super(lineNumber, direction, stopName, stopSlug);
        this.nextTime = nextTime;
    }

    public String getNextTime() {
        return nextTime;
    }

    @Override
    public String toString() {
        return super.toString() + " : " + nextTime;
    }
}

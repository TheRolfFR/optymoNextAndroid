package com.therolf.optymoNextModel;

@SuppressWarnings({"unused", "NullableProblems"})
public class OptymoDirection {
    private int lineNumber;
    private String direction;
    private String stopName;
    private String stopSlug;

    public String getStopName() {
        return stopName;
    }

    public String getStopSlug() {
        return stopSlug;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDirection() {
        return direction;
    }

    public OptymoDirection(int lineNumber, String direction, String stopName, String stopSlug) {
        this.lineNumber = lineNumber;
        this.direction = direction;
        this.stopName = stopName;
        this.stopSlug = stopSlug;
    }

    @Override
    public String toString() {
        return "[" + lineNumber + "] " + stopName + " - Dir. " + direction;
    }

    public String getLineToString() {
        return "[" + lineNumber + "] " + direction;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OptymoDirection) {
            OptymoDirection dir = (OptymoDirection) obj;
            return this.lineNumber == dir.lineNumber && this.stopSlug.equals(dir.stopSlug) && this.direction.equals(dir.direction);
        }

        return super.equals(obj);
    }
}

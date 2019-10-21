package com.therolf.optymoNextModel;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OptymoNextTime extends OptymoDirection implements Comparable<OptymoNextTime> {

    public static final String NULL_TIME_VALUE = "null";

    private String nextTime;

    public OptymoNextTime(int lineNumber, String direction, String stopName, String stopSlug, String nextTime) {
        super(lineNumber, direction, stopName, stopSlug);
        this.nextTime = nextTime;
    }

    public String getNextTime() {
        return nextTime;
    }

    public String directionToString() {
        return super.toString();
    }

    @Override
    public String toString() {
        return super.toString() + " : " + nextTime;
    }

    @Override
    public int compareTo(OptymoNextTime other) {
        if(this.nextTime.equals(NULL_TIME_VALUE))
            return +1;
        if(other.nextTime.equals(NULL_TIME_VALUE))
            return -1;

        if(this.nextTime.charAt(0) == 'A')
            return -1;
        if(other.nextTime.charAt(0) == 'A')
            return +1;

        boolean myMin = this.nextTime.contains("min");
        boolean otherMin = other.nextTime.contains("min");
        // i am coming in less than 10 min
        if(myMin) {
            // the other too, then compare the number of minutes
            if(otherMin) {
                int myNumber = Integer.parseInt(this.nextTime.split(" ")[0]);
                int otherNumber = Integer.parseInt(other.nextTime.split(" ")[0]);

                if(myNumber > otherNumber) {
                    return 1;
                }
                if(myNumber < otherNumber) {
                    return -1;
                }

                return 0;
            } else
                return -1;
        }
        // coming in more than 10 min
        else {
            // but the other not
            if(otherMin) {
                return +1;
            }
            // and the other yes, then compare hour or eventually minutes
            else {
                String[] myNumbers = this.nextTime.split(":");
                String[] otherNumbers = this.nextTime.split(":");
                if(Integer.parseInt(myNumbers[0]) < Integer.parseInt(otherNumbers[0])) {
                    return -1;
                } else if(Integer.parseInt(myNumbers[0]) > Integer.parseInt(otherNumbers[0])) {
                    return +1;
                } else {
                    if(Integer.parseInt(myNumbers[1]) < Integer.parseInt(otherNumbers[1])) {
                        return +1;
                    } else if(Integer.parseInt(myNumbers[1]) > Integer.parseInt(otherNumbers[1])) {
                        return -1;
                    }
                    return 0;
                }
            }
        }
    }
}

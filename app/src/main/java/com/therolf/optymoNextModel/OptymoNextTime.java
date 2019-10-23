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
        String myStringToCompare = this.nextTime;
        String otherStringToCompare = other.nextTime;
        if(myStringToCompare.charAt(0) == '>' && otherStringToCompare.charAt(0) == '>') {
            myStringToCompare = myStringToCompare.substring(2);
            otherStringToCompare = otherStringToCompare.substring(2);
        }
        if(myStringToCompare.charAt(0) == '>' || myStringToCompare.equals(NULL_TIME_VALUE))
            return +1;
        if(otherStringToCompare.charAt(0) == '>' || otherStringToCompare.equals(NULL_TIME_VALUE))
            return -1;

        if(myStringToCompare.charAt(0) == 'A')
            return -1;
        if(otherStringToCompare.charAt(0) == 'A')
            return +1;

        boolean myMin = myStringToCompare.contains("min");
        boolean otherMin = otherStringToCompare.contains("min");
        // i am coming in less than 10 min
        if(myMin) {
            // the other too, then compare the number of minutes
            if(otherMin) {
                int myNumber = Integer.parseInt(myStringToCompare.split(" ")[0]);
                int otherNumber = Integer.parseInt(otherStringToCompare.split(" ")[0]);

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
                String[] myNumbers = myStringToCompare.split(":");
                String[] otherNumbers = myStringToCompare.split(":");
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

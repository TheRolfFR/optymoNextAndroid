package com.therolf.optymoNextModel;

@SuppressWarnings({"unused", "WeakerAccess"})
public class OptymoNextTime extends OptymoDirection implements Comparable<OptymoNextTime> {

    public static final String NULL_TIME_VALUE = "null";
    public static final String GREATER_THAN = "> ";

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

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return super.toString() + " : " + nextTime;
    }

    @Override
    public int compareTo(OptymoNextTime other) {
        String me = this.nextTime;
        String o = other.nextTime;

        boolean me_a = me.charAt(0) == 'A';
        boolean me_min = me.contains("min");
        boolean me_hm = me.contains(":") && !me.startsWith(GREATER_THAN);
        boolean me_gt = me.startsWith(GREATER_THAN);
        boolean me_null = me.equals(NULL_TIME_VALUE);

        boolean o_a = o.charAt(0) == 'A';
        boolean o_min = o.contains("min");
        boolean o_hm = o.contains(":") && !o.startsWith(GREATER_THAN);
        boolean o_gt = o.startsWith(GREATER_THAN);
        boolean o_null = o.equals(NULL_TIME_VALUE);

        // A l'approche
        if(me_a && o_a)
            return 0;
        else if(me_a)
            return -1;
        else if(o_a)
            return +1;

        // X min
        if(me_min && o_min) {
            int myNumber = Integer.parseInt(me.split(" ")[0]);
            int otherNumber = Integer.parseInt(o.split(" ")[0]);

            if(myNumber > otherNumber) {
                return 1;
            }
            if(myNumber < otherNumber) {
                return -1;
            }

            return 0;
        } else if(me_min)
            return -1;
        else if(o_min)
            return +1;

        // XX:XX
        if(me_hm && o_hm) {
            String[] myNumbers = me.split(":");
            String[] otherNumbers = me.split(":");

            // compare hours
            if(Integer.parseInt(myNumbers[0]) < Integer.parseInt(otherNumbers[0])) {
                return -1;
            } else if(Integer.parseInt(myNumbers[0]) > Integer.parseInt(otherNumbers[0])) {
                return +1;
            } else {
                // compare minutes
                if(Integer.parseInt(myNumbers[1]) < Integer.parseInt(otherNumbers[1])) {
                    return -1;
                } else if(Integer.parseInt(myNumbers[1]) > Integer.parseInt(otherNumbers[1])) {
                    return +1;
                }
                return 0; // else time equal
            }
        } else if(me_hm)
            return -1;
        else if(o_hm)
            return +1;

        // > ?
        if(me_gt && o_gt)
            return 0;
        else if(me_gt)
            return -1;
        else if(o_gt)
            return +1;

        // null
        return 0;
    }

    public static String buildGreaterString(OptymoNextTime nextTime) {
        return GREATER_THAN + nextTime.getNextTime();
    }
}

package uk.ac.ed.inf;

/**
 * Keeps the elapsed system time from when it was started.
 */
public class TimeKeeper {
    private static TimeKeeper instance;
    private long startTime = -1; // -1 => timer has not been started yet
    private TimeKeeper() {}

    public static TimeKeeper getTimeKeeper() {
        if (instance == null) {
            instance = new TimeKeeper();
        }
        return instance;
    }

    /**
     * Starts keeping time
     */
    public void startKeepingTime() {
        startTime = System.currentTimeMillis();
    }

    public boolean isStarted() {
        return startTime != -1;
    }

    /**
     * Retrieves the current time, relative to the start time
     */
    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }
}

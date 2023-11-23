package uk.ac.ed.inf;

public class TimeKeeper {
    private static TimeKeeper instance;
    private long startTime = -1;
    private TimeKeeper() {}

    public static TimeKeeper getTimeKeeper() {
        if (instance == null) {
            instance = new TimeKeeper();
        }
        return instance;
    }

    public void startKeepingTime() {
        startTime = System.currentTimeMillis();
    }

    public boolean isStarted() {
        return startTime != -1;
    }

    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }
}

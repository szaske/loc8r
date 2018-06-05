package com.loc8r.seattle.models;


import java.util.Date;

public class DateInterval {
    private Date beginning;
    private Date ending;

    public DateInterval(Date beginning, Date ending) {
        this.beginning = beginning;
        this.ending = ending;
    }

    public boolean contains(Date date) {
        long dateEpochMillis = date.getTime();
        long thisStart = getBeginningMillis();
        long thisEnd = getEndingMillis();
        return (dateEpochMillis >= thisStart && dateEpochMillis < thisEnd);
    }

    public boolean contains(long dateEpochMillis) {
        long thisStart = getBeginningMillis();
        long thisEnd = getEndingMillis();
        return (dateEpochMillis >= thisStart && dateEpochMillis < thisEnd);
    }

    private long getEndingMillis() {
        return ending.getTime();
    }

    private long getBeginningMillis() {
        return beginning.getTime();
    }

}

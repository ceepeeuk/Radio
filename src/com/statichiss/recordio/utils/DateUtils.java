package com.statichiss.recordio.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateUtils {

    public static long addDay(long dateTimeMillis) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dateTimeMillis);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTimeInMillis();
    }

    public static long addWeek(long dateTimeMillis) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dateTimeMillis);
        calendar.add(Calendar.DATE, 7);
        return calendar.getTimeInMillis();
    }
}

package com.statichiss.recordio.utils;

import java.text.SimpleDateFormat;
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

    public static String getDateTimeString(long dateTime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm dd/MM/yy");
        StringBuilder dateString = new StringBuilder(format.format(dateTime));
        return dateString.toString();
    }

    public static String getHoursAndMinutes(long dateTimeMillis) {
        String format = String.format("%%0%dd", 2);
        long elapsedTime = dateTimeMillis / 1000;
        String minutes = String.format(format, (elapsedTime % 3600) / 60);
        String hours = String.format(format, elapsedTime / 3600);
        return hours + ":" + minutes;
    }
}

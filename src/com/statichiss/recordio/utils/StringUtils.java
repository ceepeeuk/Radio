package com.statichiss.recordio.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StringUtils {

    public static boolean IsNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String pad(int originalNum) {
        String original = String.valueOf(originalNum);
        if (original.length() == 1) {
            return "0" + original;
        } else {
            return original;
        }
    }

    public static String convertDateTimeToString(long milliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE dd/MM/yy HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }
}

package com.cpdev.utils;

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
}

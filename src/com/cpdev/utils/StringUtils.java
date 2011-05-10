package com.cpdev.utils;

public class StringUtils {

    public static boolean IsNullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
}

package com.csl.macrologandroid.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    protected DateUtil() {
        // No arg constructor
    }

    public static String format(Date date) {
        SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return standardFormat.format(date);
    }

    public static Date parse(String string) {
        SimpleDateFormat standardFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat shortFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        SimpleDateFormat reversedFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat reversedShortFormat = new SimpleDateFormat("d-M-yyyy", Locale.getDefault());

        Date date = null;

        if (string.charAt(4) == '-' && (string.charAt(6) == '-' || string.charAt(7) == '-')) {
            try {
                date = standardFormat.parse(string);
            } catch (ParseException ex) {
                try {
                    date = shortFormat.parse(string);
                } catch (ParseException ex2) {
                    Log.e(DateUtil.class.toString(), "Could not parse to Date");
                }
            }
        } else {
            try {
                date = reversedFormat.parse(string);
            } catch (ParseException ex3) {
                try {
                    date = reversedShortFormat.parse(string);
                } catch (ParseException ex4) {
                    Log.e(DateUtil.class.toString(), "Could not parse to Date");
                }
            }
        }

        return date;
    }
}

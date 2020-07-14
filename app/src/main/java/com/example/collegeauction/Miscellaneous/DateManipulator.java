package com.example.collegeauction.Miscellaneous;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateManipulator {

    Date d1;
    Date d2;
    long diff;
    long diffSeconds;
    long diffMinutes;
    long diffHours;

    public DateManipulator(Date date1){
        d1 = date1;
        // Get msec from each, and subtract.
        diff = d1.getTime() - System.currentTimeMillis();
        diffSeconds = diff / 1000 % 60;
        diffMinutes = diff / (60 * 1000);
        diffHours = diff / (60 * 60 * 1000);
    }



    public long getDiffSeconds() {
        diff = d1.getTime() - System.currentTimeMillis();
        diffSeconds = diff / 1000 % 60;
        return diffSeconds;
    }

    public long getDiffMinutes() {
        diff = d1.getTime() - System.currentTimeMillis();
        long diffMinutes = diff / (60 * 1000) % 60;
        return diffMinutes;
    }

    public long getDiffHours() {
        diff = d1.getTime() - System.currentTimeMillis();
        diffHours = diff / (60 * 60 * 1000);
        return diffHours;
    }

    public String getDate() {
        diff = d1.getTime() - System.currentTimeMillis();
        diffSeconds = diff / 1000 % 60;
        String diffSecondsStr = Long.toString(diffSeconds);
        if (diffSeconds < 10){
            diffSecondsStr = "0" + diffSecondsStr;
        }
        diffMinutes = diff / (60 * 1000) % 60;
        String diffMinutesStr = Long.toString(diffMinutes);
        if (diffMinutes < 10){
            diffMinutesStr = "0" + diffMinutesStr;
        }
        diffHours = diff / (60 * 60 * 1000);
        String diffHoursStr = Long.toString(diffHours);
        if (diffHours < 10){
            diffHoursStr = "0" + diffHoursStr;
        }
        String result = diffHoursStr + ":" + diffMinutesStr + ":" + diffSecondsStr;
        return result;
    }
}


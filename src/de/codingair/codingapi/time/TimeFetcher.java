package de.codingair.codingapi.time;

import java.util.Date;

public class TimeFetcher {
    public enum Time {
        TICKS, SECONDS, MINUTES, HOURS, DAYS, MILLISECONDS;

        public int toSeconds(int value) {
            switch(this) {
                case TICKS:
                    return value / 20;
                case MINUTES:
                    return value * 60;
                case HOURS:
                    return value * 60 * 60;
                case DAYS:
                    return value * 60 * 60 * 24;
                case MILLISECONDS:
                    return value / 1000;
                default:
                    return value;
            }
        }
    }

    public static Date getDate() {
        return new Date();
    }

    public static String dateToString(Date date) {
        return getDay(date) + "_" + (getMonthNum(date) + 1) + "_" + getYear(date) + " " + getHour(date) + ":" + getMinute(date) + ":" + getSecond(date);
    }

    public static String dateToString() {
        return dateToString(getDate());
    }

    public static Date dateFromString(String code) {
        if(code == null) return null;

        int day = Integer.parseInt(code.split("_")[0]);
        int month = Integer.parseInt(code.split("_")[1]);
        int year = Integer.parseInt(code.split("_")[2].split(" ")[0]);
        int hour = Integer.parseInt(code.split(" ")[1].split(":")[0]);
        int minute = Integer.parseInt(code.split(" ")[1].split(":")[1].split(":")[0]);
        int second = Integer.parseInt(code.split(" ")[1].split(":")[2]);

        Date date = new Date(year - 1900, month - 1, day, hour, minute, second);
        return date;
    }

    public static void addTime(Date date, Time time, int value) {
        long millis = time.toSeconds(value) * 1000;
        date.setTime(date.getTime() + millis);
    }

    public static void removeTime(Date date, Time time, int value) {
        long millis = time.toSeconds(value) * 1000;
        date.setTime(date.getTime() - millis);
    }

    public static String getClock() {
        return getClock(getDate());
    }

    public static String getClock(Date date) {
        String s;

        if(getHour(date) < 10) s = "0" + getHour(date);
        else s = "" + getHour(date);

        s = s + ":";

        if(getMinute(date) < 10) s = s + "0" + getMinute(date);
        else s = s + getMinute(date);

        return s;
    }

    public static String getWeekDay() {
        return getWeekDay(getDate());
    }

    public static String getWeekDay(Date date) {
        String time = date.toString();

        String weekDay = time.split(" ")[0];

        return weekDay;
    }

    public static String getMonth() {
        return getMonth(getDate());
    }

    public static String getMonth(Date date) {
        String time = date.toString();

        String weekDay = time.split(" ")[1];

        return weekDay;
    }

    public static int getMonthNum() {
        return getMonthNum(getDate());
    }

    public static int getMonthNum(Date date) {
        return date.getMonth();
    }

    public static int getDay() {
        return getDay(getDate());
    }

    public static int getDay(Date date) {
        String time = date.toString();

        String day_S = time.split(" ")[2];
        int day;

        try {
            day = Integer.parseInt(day_S);
        } catch(NumberFormatException ex) {
            day = -1;
        }

        return day;
    }

    public static String getTime() {
        return getTime(getDate());
    }

    public static String getTime(Date date) {
        String time = date.toString();

        return time.split(" ")[3];
    }

    public static int getHour() {
        return getHour(getDate());
    }

    public static int getHour(Date date) {
        String time = getTime(date);

        String hour_S = time.split("\\:")[0];
        int hour;

        try {
            hour = Integer.parseInt(hour_S);
        } catch(NumberFormatException ex) {
            hour = -1;
        }

        return hour;
    }

    public static int getMinute() {
        return getMinute(getDate());
    }

    public static int getMinute(Date date) {
        String time = getTime(date);

        String minute_S = time.split("\\:")[1];
        int minute;

        try {
            minute = Integer.parseInt(minute_S);
        } catch(NumberFormatException ex) {
            minute = -1;
        }

        return minute;
    }

    public static int getSecond() {
        return getSecond(getDate());
    }

    public static int getSecond(Date date) {
        String time = getTime(date);

        String second_S = time.split("\\:")[2];
        int second;

        try {
            second = Integer.parseInt(second_S);
        } catch(NumberFormatException ex) {
            second = -1;
        }

        return second;
    }

    public static int getYear() {
        return getYear(getDate());
    }

    public static int getYear(Date date) {
        String time = date.toString();

        String year_S = time.split(" ")[5];
        int year;

        try {
            year = Integer.parseInt(year_S);
        } catch(Exception ex) {
            year = -1;
        }

        return year;
    }

    public static String[] secToTimeString(int sec) {
        int min = sec / 60;
        sec = sec - min * 60;

        String s = "", m = "";

        if(min < 0) min = 0;
        if(min < 10) m = "0";
        m = m + min;

        if(sec < 0) sec = 0;
        if(sec < 10) s = "0";
        s = s + sec;

        String[] a = new String[2];

        a[0] = m;
        a[1] = s;

        return a;
    }
}

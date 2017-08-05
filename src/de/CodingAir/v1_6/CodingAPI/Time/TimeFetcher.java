package de.CodingAir.v1_6.CodingAPI.Time;

import java.util.Date;

public class TimeFetcher {
	public enum Time {
		TICKS, SECONDS, MINUTES, HOURS, DAYS, MILLISECONDS;
		
		public int toSeconds(int value) {
			switch(this) {
				case TICKS: return value / 20;
				case MINUTES: return value * 60;
				case HOURS: return value * 60 * 60;
				case DAYS: return value * 60 * 60 * 24;
				case MILLISECONDS: return value / 1000;
				default: return value;
			}
		}
	}
	
	public static Date getDate() {
		return new Date();
	}
	
	public static String dateToString() {
		return getDay() + "_" + (getMonthNum() + 1) + "_" + getYear() + " " + getHour() + ":" + getMinute() + ":" + getSecond();
	}
	
	public static String dateToString(Date date) {
		return date.getDay() + "_" + (date.getMonth() + 1) + "_" + date.getYear() + " " + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
	}
	
	public static Date dateFromString(String code) {
		int day = Integer.parseInt(code.split("_")[0]);
		int month = Integer.parseInt(code.split("_")[1]) - 1;
		int year = Integer.parseInt(code.split("_")[2].split(" ")[0]);
		int hour = Integer.parseInt(code.split(" ")[1].split(":")[0]);
		int minute = Integer.parseInt(code.split(" ")[1].split(":")[1].split(":")[0]);
		int second = Integer.parseInt(code.split(" ")[1].split(":")[2]);
		
		
		
		Date date =  new Date(year, month, day, hour, minute, second);
		System.out.println(dateToString(date));
		
		return date;
	}
	
	public static String getClock() {
		String s;
		
		if(getHour() < 10) s = "0" + getHour();
		else s = "" + getHour();
		
		s = s + ":";
		
		if(getMinute() < 10) s = s + "0" + getMinute();
		else s = s + getMinute();
		
		return s;
	}
	
	public static String getWeekDay() {
		String time = getDate().toString();
		
		String weekDay = time.split(" ")[0];
		
		return weekDay;
	}
	
	public static String getMonth() {
		String time = getDate().toString();
		
		String weekDay = time.split(" ")[1];
		
		return weekDay;
	}
	
	public static int getMonthNum() {
		return getDate().getMonth();
	}
	
	public static int getDay() {
		String time = getDate().toString();
		
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
		String time = getDate().toString();
		
		return time.split(" ")[3];
	}
	
	public static int getHour() {
		String time = getTime();
		
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
		String time = getTime();
		
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
		String time = getTime();
		
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
		String time = getDate().toString();
		
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

package com.globalLibrary.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeFormatUtil {
	private static final SimpleDateFormat short_format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	private static final SimpleDateFormat long_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
	
	public static String getShortFormatDateString(Date date) {
		return short_format.format(date);
	}
	public static String getLongFormatDateString(Date date) {
		return long_format.format(date);
	}
	
	public static String millisecondsToFormat(long milliseconds, SimpleDateFormat format){
		Date date = new Date(milliseconds);
		return format.format(date);
	}
}

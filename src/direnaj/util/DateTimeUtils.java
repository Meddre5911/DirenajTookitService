
package direnaj.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class DateTimeUtils {

	public static final String DEFAULT_TIMEZONE = "Europe/Istanbul";
	private static TimeZone localTZ = TimeZone.getTimeZone(DEFAULT_TIMEZONE);
	public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String TWITTER_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
	public static final SimpleDateFormat STANDARD_SIMPLE_DATE_FORMAT = new SimpleDateFormat(STANDARD_FORMAT);

	public static Date getTwitterDate(String date) throws Exception {
		if (!TextUtils.isEmpty(date)) {
			// Example : Thu Nov 03 11:37:45 +0000 2011
			SimpleDateFormat sf = new SimpleDateFormat(TWITTER_FORMAT, Locale.ENGLISH);
			sf.setLenient(false);
			return sf.parse(date);
		}
		return null;
	}

	public static Date getDate(String dateFormatInStr, String dateInStr) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormatInStr);
		Date date = formatter.parse(dateInStr);
		return date;
	}

	public static String getStringOfDate(String dateFormatInStr, Date date) throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormatInStr);
		String reportDate = formatter.format(date);
		return reportDate;
	}

	public static Date getTwitterDateFromRataDieFormat(String dateInRataDie) throws Exception {
		BigDecimal date = new BigDecimal(dateInRataDie);
		BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
		BigDecimal subtract = date.subtract(daysNumberUntilJan1970);
		BigDecimal dateInMiliSeconds = subtract.multiply(new BigDecimal("86400000"));
		long longValue = dateInMiliSeconds.longValue();
		return new Date(longValue);
	}

	public static Date getUTCDateFromRataDieFormat(String dateInRataDie) throws Exception {
		BigDecimal date = new BigDecimal(dateInRataDie);
		BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
		BigDecimal subtract = date.subtract(daysNumberUntilJan1970);
		BigDecimal dateInMiliSeconds = subtract.multiply(new BigDecimal("86400000"));
		long longValue = dateInMiliSeconds.longValue();
		return getUTCDateTime(new Date(longValue));
	}

	public static double getRataDieFormat4Date(Date date) {
		BigDecimal time = new BigDecimal(date.getTime());
		BigDecimal divide = time.divide(new BigDecimal("86400000"), 10, RoundingMode.CEILING);
		BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
		BigDecimal actualDate = daysNumberUntilJan1970.add(divide);
		return actualDate.doubleValue();
	}

	public static double getUTCRataDieFormat4Date(Date date) {
		Date utcDateTime = getUTCDateTime(date);
		BigDecimal time = new BigDecimal(utcDateTime.getTime());
		BigDecimal divide = time.divide(new BigDecimal("86400000"), 10, RoundingMode.CEILING);
		BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
		BigDecimal actualDate = daysNumberUntilJan1970.add(divide);
		return actualDate.doubleValue();
	}

	public static Date getLocalDate() {
		return new Date();
	}

	public static double getLocalDateInRataDieFormat() {
		return getRataDieFormat4Date(new Date());
	}

	public static Date getDate(Object obj) {
		try {
			if (obj != null) {
				return (Date) obj;
			}
			return null;
		} catch (Exception e) {
			Logger.getLogger(DateTimeUtils.class).error("Error During Conversion of Object to Date", e);
			return null;
		}
	}

	public static double subtractWeeksFromDate(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime minusWeeks = dateTime.minusWeeks(i);
		Date dateAfterProcess = minusWeeks.toDate();
		return getRataDieFormat4Date(dateAfterProcess);
	}

	public static Date subtractWeeksFromDateInDateFormat(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime minusWeeks = dateTime.minusWeeks(i);
		Date dateAfterProcess = minusWeeks.toDate();
		return dateAfterProcess;
	}

	public static Date subtractDaysFromDateInDateFormat(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime minusDays = dateTime.minusDays(i);
		Date dateAfterProcess = minusDays.toDate();
		return dateAfterProcess;
	}

	public static double addWeeksToDate(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime plusWeeks = dateTime.plusWeeks(i);
		Date dateAfterProcess = plusWeeks.toDate();
		return getRataDieFormat4Date(dateAfterProcess);
	}
	
	public static double addDaysToDate(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime plusDays = dateTime.plusDays(i);
		Date dateAfterProcess = plusDays.toDate();
		return getRataDieFormat4Date(dateAfterProcess);
	}
	
	public static Date addDaysToDateInDateFormat(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime plusDays = dateTime.plusDays(i);
		Date dateAfterProcess = plusDays.toDate();
		return dateAfterProcess;
	}

	public static Date addHoursToDate(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime plusWeeks = dateTime.plusHours(i);
		Date dateAfterProcess = plusWeeks.toDate();
		return dateAfterProcess;
	}

	public static Date addWeeksToDateInDateFormat(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime plusWeeks = dateTime.plusWeeks(i);
		Date dateAfterProcess = plusWeeks.toDate();
		return dateAfterProcess;
	}

	public static Date getUTCNowDateTime() {
		Date now = new Date();
		now.setTime(now.getTime() - localTZ.getOffset(now.getTime()));
		return now;
	}

	public static Date getUTCDateTime(String datetime) {
		try {
			Date d = new SimpleDateFormat(STANDARD_FORMAT).parse(datetime);
			return new Date(d.getTime() - localTZ.getOffset(d.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date getUTCDateTime(String datetime, long utcOffSet) {
		try {
			Date d = new SimpleDateFormat(STANDARD_FORMAT).parse(datetime);
			return new Date(d.getTime() - utcOffSet);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date getUTCDateTimeWithOffset(Date inpDate) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(inpDate.getTime());
		return getUTCDateTime(c, inpDate.getTimezoneOffset() * 60000);
	}

	public static Date getUTCDateTime(Date inpDate) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(inpDate.getTime());
		return getUTCDateTime(c);
	}

	public static String getUTCDateTimeStringInGenericFormat(Date inpDate) {
		if (inpDate == null)
			return "";

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(inpDate.getTime());
		Date utcDateTime = getUTCDateTime(c);

		String original = utcDateTime.toString();
		String firstPart = original.substring(0, 19);

		int length = original.length();
		String secondPart = original.substring(length - 4);

		return firstPart + " GMT " + secondPart;
	}

	public static Date getUTCDateTime(Calendar c) {
		return new Date(c.getTimeInMillis() - localTZ.getOffset(c.getTimeInMillis()));
	}

	public static Date getUTCDateTime(Calendar c, long utcOffSet) {
		return new Date(c.getTimeInMillis() + utcOffSet);
	}

	/**
	 * Returns UTC datetime from formatted as df:DateFormat datetime string
	 * according to localTZ TimeZone. Used in DB insert / update statements.
	 * 
	 * @param datetime
	 * @param df
	 * @return
	 */
	public static Date getUTCDateTime(String datetime, DateFormat df) {
		try {
			Date d = df.parse(datetime);
			return new Date(d.getTime() - localTZ.getOffset(d.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns Local date according to the given UTC date.
	 * 
	 * @param utcDate
	 * @return
	 */
	public static Date getLocalDateFromUTCDate(Date utcDate) {
		if (utcDate == null) {
			return null;
		}
		return new Date(utcDate.getTime() + localTZ.getOffset(utcDate.getTime()));
	}

	/**
	 * @param dateTime
	 * @param format
	 * @return
	 */
	public static String getLocalDateFromUTCDate(Date dateTime, String format) {
		if (dateTime == null || format == null)
			return null;
		SimpleDateFormat formatTime = new SimpleDateFormat(format);
		formatTime.setTimeZone(localTZ);
		return (formatTime.format(getLocalDateFromUTCDate(dateTime)));
	}

	/**
	 * Returns formatted string according to localTZ TimeZone. UTC to LOCAL.
	 * Used in DB read statements.
	 * 
	 * @param timestamp
	 * @param format
	 * @return
	 */
	public static String getFormattedDateFromUTC(Date dateTime, DateFormat format) {
		return format.format(new Date(dateTime.getTime() + localTZ.getOffset(dateTime.getTime())));
	}

	public static void main(String[] args) throws Exception {

//		String rataDie = "736626.3775";
//		System.out.println(DateTimeUtils.getTwitterDateFromRataDieFormat(rataDie));
//		System.out.println(DateTimeUtils.getUTCDateFromRataDieFormat(rataDie));
//		System.out.println(DateTimeUtils.getUTCDateTimeStringInGenericFormat(DateTimeUtils.getUTCDateFromRataDieFormat(rataDie)));
//		
		// "lowerTimeInterval" : "Tue Nov 15 01:00:00 GMT 2016",
		// "upperTimeInterval" : "Tue Nov 15 02:00:00 GMT 2016",

		
//        "lowerTimeInterval" : "Mon Nov 14 22:00:00 GMT 2016",
//        "upperTimeInterval" : "Mon Nov 14 23:00:00 GMT 2016",

		
		Date lowerDate = DateTimeUtils.getTwitterDate("Mon Nov 14 22:00:00 GMT 2016");
		Date upperDate = DateTimeUtils.getTwitterDate("Mon Nov 14 23:00:00 GMT 2016");
		
		
		double rataDieFormat4Date = DateTimeUtils.getRataDieFormat4Date(lowerDate);
		System.out.println("Lower : " + lowerDate + " - " + rataDieFormat4Date);
		System.out.println("Upper : " + upperDate + " - " + DateTimeUtils.getRataDieFormat4Date(upperDate));
		
		
		System.out.println(DateTimeUtils.getTwitterDateFromRataDieFormat(String.valueOf(rataDieFormat4Date)));
		System.out.println(DateTimeUtils.getUTCDateFromRataDieFormat(String.valueOf(rataDieFormat4Date)));
		
		
//		DateTime date = new DateTime(lowerDate);
//		DateTime startOfDay = date.withTimeAtStartOfDay();
//		DateTime endOfDay = date.withTime(23, 59, 59, 999);
//		
//		
//		System.out.println(startOfDay.toDate());
//		System.out.println(endOfDay.toDate());
//		
//		
//		System.out.println("Start : " + DateTimeUtils.getRataDieFormat4Date(startOfDay.toDate()));
//		System.out.println("End : " + DateTimeUtils.getRataDieFormat4Date(endOfDay.toDate()));
//		
		

	}
}

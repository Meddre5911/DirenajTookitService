package direnaj.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

public class DateTimeUtils {

	public static Date getTwitterDate(String date) throws Exception {
		// Example : Thu Nov 03 11:37:45 +0000 2011
		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
		sf.setLenient(false);
		return sf.parse(date);
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

	public static double getRataDieFormat4Date(Date date) {
		BigDecimal time = new BigDecimal(date.getTime());
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

	public static double addWeeksToDate(Date date, int i) {
		DateTime dateTime = new DateTime(date);
		DateTime plusWeeks = dateTime.plusWeeks(i);
		Date dateAfterProcess = plusWeeks.toDate();
		return getRataDieFormat4Date(dateAfterProcess);
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

	public static void main(String[] args) throws Exception {
		Date localDate = DateTimeUtils.getLocalDate();
		DateTimeUtils.getStringOfDate("yyyy-MM-dd", localDate);
		
		Date date = DateTimeUtils.getDate("yyyy-MM-dd",  "2016-06-15");
		System.out.println(date);

	}
}

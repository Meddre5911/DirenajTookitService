package direnaj.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;

public class DateTimeUtils {

	public static Date getTwitterDate(String date) throws Exception {
		// Example : Thu Nov 03 11:37:45 +0000 2011
		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
		sf.setLenient(false);
		return sf.parse(date);
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
		Date twitterDate = DateTimeUtils.getTwitterDate("Thu May 19 07:30:04 +0000 2016");
		

		System.out.println("Rata Date Back : " + DateTimeUtils.getRataDieFormat4Date(twitterDate));
	}
}

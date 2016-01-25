package direnaj.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.DateTime;

public class DateTimeUtils {

    public static Date getTwitterDate(String date) throws Exception {
        final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy"; // Example : Thu Nov 03 11:37:45 +0000 2011
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
        return date;
    }

    public static double addWeeksToDate(Date date, int i) {
        DateTime dateTime = new DateTime(date);
        DateTime plusWeeks = dateTime.plusWeeks(i);
        Date dateAfterProcess = plusWeeks.toDate();
        return getRataDieFormat4Date(dateAfterProcess);
    }
    
    public static Date addWeeksToDateInDateFormat(Date date, int i) {
        DateTime dateTime = new DateTime(date);
        DateTime plusWeeks = dateTime.plusWeeks(i);
        Date dateAfterProcess = plusWeeks.toDate();
        return dateAfterProcess;
    }

    public static void main(String[] args) throws Exception {
        BigDecimal date = new BigDecimal("735543.7574768518");
        BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
        BigDecimal subtract = date.subtract(daysNumberUntilJan1970);
        BigDecimal dateInMiliSeconds = subtract.multiply(new BigDecimal("86400000"));
        System.out.println("Date In miliseconds : " + dateInMiliSeconds);
        long longValue = dateInMiliSeconds.longValue();
        System.out.println("Long Value : " + longValue);
        Date julianDate = new Date(longValue);
        System.out.println("Date : " + julianDate);
        System.out.println("Twitter Date : " + DateTimeUtils.getTwitterDate("Tue Nov 05 20:10:45 EET 2013"));

        System.out.println("Rata Date Back : " + getRataDieFormat4Date(julianDate));
    }
}

package direnaj.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static Date getTwitterDate(String date) throws Exception {
        final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(false);
        return sf.parse(date);
    }

    public static Date getTwitterDateFromRateDieFormat(String dateInRataDie) throws Exception {
        BigDecimal date = new BigDecimal(dateInRataDie);
        BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
        BigDecimal subtract = date.subtract(daysNumberUntilJan1970);
        BigDecimal dateInMiliSeconds = subtract.multiply(new BigDecimal("86400000"));
        long longValue = dateInMiliSeconds.longValue();
        return new Date(longValue);
    }

    public static Date getLocalDate() {
        return new Date();
    }

    public static void main(String[] args) {
        BigDecimal date = new BigDecimal("735543.7574768518");
        BigDecimal daysNumberUntilJan1970 = new BigDecimal("719529.0");
        BigDecimal subtract = date.subtract(daysNumberUntilJan1970);
        BigDecimal dateInMiliSeconds = subtract.multiply(new BigDecimal("86400000"));
        System.out.println("Date In miliseconds : " + dateInMiliSeconds);
        long longValue = dateInMiliSeconds.longValue();
        System.out.println("Long Value : " + longValue);
        System.out.println("Date : " + new Date(longValue));
    }
}

package direnaj.util;

public class URLUtil {

    public static String getURLDomainName(String url) {
        String domainName = "";
        try {
            String[] split = url.split("://");
            int indexOf = split[1].indexOf("/");
            domainName = split[1].substring(0, indexOf);
        } catch (Exception e) {
        }
        return domainName;
    }

}

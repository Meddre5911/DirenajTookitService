package direnaj.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class HTTPUtil {

    public static String sendGetRequest(String requestString) throws Exception {
        // check whether URL is HTTPS or not
        boolean isHttps = false;
        String[] split = requestString.split(":");
        if (split.length > 0 && split[0].equals("https")) {
            isHttps = true;
        }
        URL myurl = new URL(requestString);
        URLConnection con = null;
        if (isHttps) {
            con = (HttpsURLConnection) myurl.openConnection();
        } else {
            con = (HttpURLConnection) myurl.openConnection();
        }

        InputStream ins = con.getInputStream();
        InputStreamReader isr = new InputStreamReader(ins);
        BufferedReader in = new BufferedReader(isr);

        String inputLine;
        String responseStr = "";
        while ((inputLine = in.readLine()) != null) {
            responseStr += inputLine;
        }
//        System.out.println("Response : " + responseStr);
        in.close();
        return responseStr;
    }

    public static void main(String[] args) throws Exception {
        //        String str = HTTPUtil.sendGetRequest("http://api.longurl.org/v2/expand?url=http%3A%2F%2Fis.gd%2Fw");
        //        int beginning = str.lastIndexOf("[");
        //        int lastIndex = str.lastIndexOf("]");
        //        System.out.println(str.substring(beginning+1, lastIndex-1));

        //        String str = HTTPUtil.sendGetRequest("http://api.longurl.org/v2/services?format=json");
        //        JSONObject json = new JSONObject(str);
        //        JSONArray names = json.names();
        //        for (int i = 0; i < names.length(); i++) {
        //            System.out.println(names.getString(i));
        //        }

        //        String url = "http://t.co/3VuBmkY/uRp";
        //        String[] split = url.split("://");
        //        int indexOf = split[1].indexOf("/");
        //        String serviceProvider = split[1].substring(0, indexOf);
        //        System.out.println(serviceProvider);

//        String requestedURL = "http://www.milliyet.com.tr/";
//        String encodedUrl = URLEncoder.encode(requestedURL, "UTF-8");
//
//        String str = HTTPUtil
//                .sendGetRequest("https://sb-ssl.google.com/safebrowsing/api/lookup?client=api&apikey=ABQIAAAAQnXmJsd0dfyNH4ko7cGHzxR6Ob1nfPRsiZxBAkhmy1f6vKkanw&appver=1.0&pver=3.0&url="
//                        + encodedUrl);

    }
}
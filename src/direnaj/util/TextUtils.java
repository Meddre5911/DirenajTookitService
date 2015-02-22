package direnaj.util;

public class TextUtils {

    public static boolean isEmpty(String str) {
        if (str != null && !str.trim().equals("") && !str.trim().equals("null")) {
            return false;
        }
        return true;
    }

}

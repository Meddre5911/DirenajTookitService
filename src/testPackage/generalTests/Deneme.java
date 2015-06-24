package testPackage.generalTests;

public class Deneme {
    
    public static void main(String[] args) {
        String str = "<a href=\"http://linkis.com\" rel=\"nofollow\">Windows Twitter for</a>";
        int indexOf = str.indexOf(">");
        String newStr = str.substring(indexOf);
        System.out.println("New Str : " + newStr);
        int indexOf2 = newStr.indexOf("<");
        String substring = newStr.substring(1, indexOf2);
        System.out.println("Final Str : " + substring);
        
        int indexOf3 = substring.indexOf("Twitter for");
        System.out.println(indexOf3);
    }

}

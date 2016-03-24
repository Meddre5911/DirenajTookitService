package testPackage.generalTests;

public class Deneme {
    
    public static void main(String[] args) {
        String str = "<a href=\"http://linkis.com\" rel=\"nofollow\">Windows .Twitter for</a>";
        String str2 = "$erde";
        
       
        
        if(str.contains(".")){
        	String newStr = str.replace('.', '_');
        	System.out.println("New Str : " + newStr);
        }
        
        if(str2.startsWith("$")){
        	String newStr = str2.substring(1);
        	System.out.println("New Str 2 : " + newStr);
        }
        
    }

}

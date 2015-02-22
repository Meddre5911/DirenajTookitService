package testPackage.generalTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import direnaj.domain.User;
import direnaj.functionalities.sna.CentralityTypes;

public class GeneralTester {
	
	public String testGetTags(ArrayList<ArrayList<String>> allTags) {
	    String retHtmlStr = "<ul>";
	    for (ArrayList<String> singleTweetTags : allTags) {
	        // if(singleTweetTags.size() != 0) {
	        retHtmlStr += "<li>";
	        for (String tag : singleTweetTags) {
	            retHtmlStr += tag + " - ";
	        }
	        retHtmlStr += "</li>";
	        // }
	    }
	    retHtmlStr += "</ul>";
	    
	    return retHtmlStr;
	}
	
	public String testTagCounter(ArrayList<Entry<String, Integer>> counts) throws IOException, InterruptedException {
	
		PrintWriter pw = new PrintWriter(new File("/tmp/tags.txt"));
	
	    boolean skipFirst = true;
	
	    String retHtmlStr = "<table width=100% border=0><tr valign=top><td><ul>";
	    for (Map.Entry<String, Integer> entry : counts) {
	        retHtmlStr += "<li><font size=\"2\">(" + entry.getKey() + " : " + entry.getValue()
	                + ")</font></li>";
	
	        if (skipFirst) {
	            skipFirst = false;
	            continue;
	        } else {
	            pw.println(entry.getKey() + "\t" + entry.getValue());
	        }
	    }
	    retHtmlStr += "</ul></td><td align=right>";
	
	    pw.close();
	
	    String cmd = "java -jar /home/direnaj/direnaj/envs/staging/toolkit/DirenajToolkitService/WebContent/WEB-INF/lib/ibm-word-cloud.jar "
	            + "-c /home/direnaj/direnaj/tools/ibm-word-cloud.conf -w 800 -h 600 "
	            + "-i /tmp/tags.txt "
	            + "-o /home/direnaj/direnaj/tools/images/caner.png";
	
	    Process proc = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
	
	    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	
	    String line = null;
	    while ((line = br.readLine()) != null) {
	        System.out.println(line);
	    }
	
	    proc.waitFor();
	
	    retHtmlStr += "<img src=\"images/caner.png\"/>   </td></tr></table>";
	    
	    return retHtmlStr;
	}
	
	public String testTweetTextGetter(ArrayList<String> tweetTexts) {
		
		String retHtmlStr = "<ul>";
	    for (String tweetText : tweetTexts) {
	        retHtmlStr += "<li><font size=\"2\">" + tweetText + "</font></li>";
	    }
	    retHtmlStr += "</ul>";
	    return retHtmlStr;
	}
	
	
	public String testFreqUser(ArrayList<Entry<User, Integer>> distinctUserPostCounts) {
		
		String retHtmlStr = "<b> Total Distinct User Count is " + distinctUserPostCounts.size() + "</b></p>";
	    for (Entry<User, Integer> entry : distinctUserPostCounts) {
	
	        retHtmlStr += "<li><font size=\"2\">(" + ((User) entry.getKey()).getUserScreenName() + " : "
	                + entry.getValue() + ")</font></li>";
	    }
	    return retHtmlStr;
	}
	
	public String testFreqMentionedUser(ArrayList<Entry<User, Integer>> distinctMentionedUserCounts) {
	
		String retHtmlStr = "<b> Total Distinct Mentioned User Count is " + distinctMentionedUserCounts.size()
	            + "</b></p>";
	    for (Map.Entry<User, Integer> entry : distinctMentionedUserCounts) {
	
	        retHtmlStr += "<li><font size=\"2\">(" + entry.getKey().getUserScreenName() + " : "
	                + entry.getValue() + ")</font></li>";
	    }
	    
	    return retHtmlStr;
	}
	
	public String testFreqURL(ArrayList<Entry<String, Integer>> distinctUrlCounts) {
	
		String retHtmlStr = "<b> Total Distinct URL Count is " + distinctUrlCounts.size() + "</b></p>";
	    for (Map.Entry<String, Integer> entry : distinctUrlCounts) {
	
	        retHtmlStr += "<li><font size=\"2\">(" + entry.getKey() + " : " + entry.getValue()
	                + ")</font></li>";
	    }
    	return retHtmlStr;
	}
	
	public String testCentrality(Map<CentralityTypes, ArrayList<Entry<User, BigDecimal>>> centralitiesOfUsers) {
		
        
		String retHtmlStr = "<b> Centralities of Users </b></p>";
        retHtmlStr += "<table>";
        ArrayList<Entry<User, BigDecimal>> betweennessCentralities = centralitiesOfUsers
                .get(CentralityTypes.Betweenness);
        ArrayList<Entry<User, BigDecimal>> inDegreeCentralities = centralitiesOfUsers
                .get(CentralityTypes.InDegree);
        // get max count
        int maxUserCount = Math.max(betweennessCentralities.size(), inDegreeCentralities.size());
        Iterator<Entry<User, BigDecimal>> inDegreeIterator = inDegreeCentralities.iterator();
        Iterator<Entry<User, BigDecimal>> betweennessIterator = betweennessCentralities.iterator();
        retHtmlStr += "<tr><td><b>Betweenness Calculations</b></td>";
        retHtmlStr += "<td><b>Indegree Calculations</td></b></tr>";
        for (int i = 0; i < maxUserCount; i++) {
            retHtmlStr += "<tr><td>";
            if (betweennessIterator.hasNext()) {
                Entry<User, BigDecimal> next = betweennessIterator.next();
                retHtmlStr += next.getKey().getUserScreenName() + " - " + next.getValue();
            }
            retHtmlStr += "</td><td>";
            if (inDegreeIterator.hasNext()) {
                Entry<User, BigDecimal> next = inDegreeIterator.next();
                retHtmlStr += next.getKey().getUserScreenName() + " - " + next.getValue();
            }
            retHtmlStr += "</td></tr>";
        }
        retHtmlStr += "</table>";
        
        return retHtmlStr;
	}
	
}

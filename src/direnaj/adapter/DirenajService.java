package direnaj.adapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import direnaj.domain.DirenajObjects;
import direnaj.domain.User;
import direnaj.driver.DirenajDriver;
import direnaj.functionalities.sna.CentralityAnalysis;
import direnaj.functionalities.sna.CentralityTypes;


/**
 * Servlet implementation class DirenajService
 */
@WebServlet("/DirenajService")
public class DirenajService extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DirenajService() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json; charset=UTF-8");
        PrintWriter printout = response.getWriter();

                     // getting parameters from html form
        Map<String, String[]> params = request.getParameterMap();

        String userId = params.get("userID")[0];
		String password = params.get("pass")[0];
		String campaignId = params.get("campaignID")[0];
		int skip = Integer.parseInt(params.get("skip")[0]);
		int limit = Integer.parseInt(params.get("limit")[0]);

		String operation = params.get("operationType")[0];


		DirenajDriver driver = new DirenajDriver(userId, password);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		ArrayList<ArrayList<String>> allTags = new ArrayList<ArrayList<String>>();
		ArrayList<Map.Entry<String, Integer>> counts = new ArrayList<Map.Entry<String, Integer>>();
		ArrayList<String> tweetTexts = new ArrayList<String>();
		
		try {
			allTags = driver.collectHashtags(campaignId, skip, limit);
			
			 if (operation.equals("getHashTags")) {

	            	allTags = driver.collectHashtags(campaignId, skip, limit);
	            	
	            	
	            	
	            	printout.println(gson.toJson(allTags));
	            	
			 } else if (operation.equals("getHashTagCounts")) {

	                counts = driver.countHastags(campaignId, skip, limit);

	                printout.println(gson.toJson(counts));
	                
	            } else if (operation.equals("getTweetTexts")) {

	                tweetTexts = driver.collectTweetTexts(campaignId, skip, limit);

	                printout.println(gson.toJson(tweetTexts));
	                
	            } else if (operation.equals("getSingleTweet")) {
	            	/*
	            	String jsonStr = gson.toJson(driver.getSingleTweetInfo(campaignId, skip, limit));
	            	byte[] utf8JsonStr = jsonStr.getBytes("UTF8");
	                printout.write(new String(utf8JsonStr));
					*/
	            	printout.println("not supported yet");
	            } else if (operation.equals("getFrequentUsers")) {
	                ArrayList<Entry<User, Integer>> distinctUserPostCounts = driver.getBulkDistinctDomainObjectCount(
	                        campaignId, skip, limit, DirenajObjects.User);
	                	
	                printout.println(gson.toJson(distinctUserPostCounts));
	                
	            } else if (operation.equals("getFrequentMentionedUsers")) {
	                ArrayList<Entry<User, Integer>> distinctMentionedUserCounts = driver.getBulkDistinctDomainObjectCount(
	                        campaignId, skip, limit, DirenajObjects.MentionedUser);
	                
	                //retHtmlStr += tester.testFreqMentionedUser(distinctMentionedUserCounts);
	                printout.println(gson.toJson(distinctMentionedUserCounts));
	                
	            } else if (operation.equals("getFrequentUrls")) {
	                ArrayList<Entry<String, Integer>> distinctUrlCounts = driver.getBulkDistinctDomainObjectCount(
	                        campaignId, skip, limit, DirenajObjects.Url);
	                
	                //retHtmlStr += tester.testFreqURL(distinctUrlCounts);
	                printout.println(gson.toJson(distinctUrlCounts));
	                
	            } else if (operation.equals("getUserCentralities")) {
	            	Map<CentralityTypes, ArrayList<Entry<User, BigDecimal>>> centralitiesOfUsers = CentralityAnalysis
	                        .calculateCentralityOfUsers(userId, password, campaignId, skip, limit);
	            	
	            	//retHtmlStr += tester.testCentrality(centralitiesOfUsers);
	            	printout.println(gson.toJson(centralitiesOfUsers));
	            	
	            } else if (operation.equals("getHashtagTimeline")) {
	            	printout.println("Not Supported Yet.");
        			/*
	                request.setAttribute("campaignId", campaignId);
	                request.setAttribute("operation", operation);
	                request.setAttribute("limit", limit);
	                counts = driver.countHastags(campaignId, skip, limit);
	                request.setAttribute("hashtagCounts", counts);
	                ServletContext context = getServletContext();
	                RequestDispatcher dispatcher = context.getRequestDispatcher("/hashtagTimeLineRequest.jsp");
	                dispatcher.forward(request, response);
	                */
	            } else {
	                printout.println("OPERATION NOT SUPPORTED!");
	            }
			
			
		} catch (DirenajInvalidJSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
            printout.println(e.getMessage());
            printout.close();
            e.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

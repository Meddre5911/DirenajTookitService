package testPackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import testPackage.generalTests.GeneralTester;
import direnaj.domain.DirenajObjects;
import direnaj.domain.User;
import direnaj.driver.DirenajDriver;
import direnaj.functionalities.graph.DirenajGraph;
import direnaj.functionalities.graph.GraphUtil;
import direnaj.functionalities.graph.Relations;
import direnaj.functionalities.sna.BotAccountCalculationWeight;
import direnaj.functionalities.sna.CentralityAnalysis;
import direnaj.functionalities.sna.CentralityTypes;
import direnaj.functionalities.sna.UserAccountPropertyAnalyser;
import direnaj.functionalities.sna.communityDetection.CommunityDetector;
import direnaj.functionalities.sna.communityDetection.DetectedCommunities;
import direnaj.util.CollectionUtil;
import direnaj.util.TextUtils;
import direnaj.util.d3Lib.D3GraphicType;

/**
 * Simple servlet for testing.
 * 
 * Uses DirenajDataHandler from direnajAdapter package
 */

// @WebServlet("/test")
public class TestServlet extends HttpServlet {
    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // getting parameters from html form
        Map<String, String[]> params = request.getParameterMap();

        String userId = params.get("userID")[0];
        String password = params.get("pass")[0];
        String campaignId = params.get("campaignID")[0];
        int skip = Integer.parseInt(params.get("skip")[0]);
        int limit = Integer.parseInt(params.get("limit")[0]);

        HttpSession session = request.getSession(true);
        session.setAttribute("userID", userId);
        session.setAttribute("pass", password);
        session.setAttribute("campaignID", campaignId);
        session.setAttribute("skip", skip);
        session.setAttribute("limit", limit);

        String operation = params.get("operationType")[0];

        System.out.println(operation);

        // Start preparing objects

        // constructing the driver object that will handle our data retrieval
        // and processing requests, using DirenajDataHandler as a backbone
        DirenajDriver driver = new DirenajDriver(userId, password);

        GeneralTester tester = new GeneralTester();

        ArrayList<String> tweetTexts = new ArrayList<String>();
        ArrayList<ArrayList<String>> allTags = new ArrayList<ArrayList<String>>();
        ArrayList<Map.Entry<String, Integer>> counts = new ArrayList<Map.Entry<String, Integer>>();

        // building the result page

        String retHtmlStr = "<!DOCTYPE html>\n" + "<html>\n" + "<head><title>Direnaj Test Center</title></head>\n"
                + "<body bgcolor=\"#fdf5e6\">\n" + "<h1>DirenajDriver Test</h1>\n" + "<p>CampaignID : <b>" + campaignId
                + "</b> | Operation : <b>" + operation + "</b> | Limit : <b>" + limit + "</b></p><hr>\n";

        try {
            if (operation.equals("getTags")) {

                allTags = driver.collectHashtags(campaignId, skip, limit);

                retHtmlStr += tester.testGetTags(allTags);

            } else if (operation.equals("getTagCounts")) {

                counts = driver.countHastags(campaignId, skip, limit);

                retHtmlStr += tester.testTagCounter(counts);

            } else if (operation.equals("getTweetTexts")) {

                tweetTexts = driver.collectTweetTexts(campaignId, skip, limit);

                retHtmlStr += tester.testTweetTextGetter(tweetTexts);

            } else if (operation.equals("getSingleTweet")) {

                retHtmlStr += driver.getSingleTweetInfo(campaignId, skip, limit);

            } else if (operation.equals("getFrequentUsers")) {
                ArrayList<Entry<User, Integer>> distinctUserPostCounts = driver.getBulkDistinctDomainObjectCount(
                        campaignId, skip, limit, DirenajObjects.User);

                retHtmlStr += tester.testFreqUser(distinctUserPostCounts);

            } else if (operation.equals("getFrequentMentionedUsers")) {
                ArrayList<Entry<User, Integer>> distinctMentionedUserCounts = driver.getBulkDistinctDomainObjectCount(
                        campaignId, skip, limit, DirenajObjects.MentionedUser);

                retHtmlStr += tester.testFreqMentionedUser(distinctMentionedUserCounts);

            } else if (operation.equals("getFrequentUrls")) {
                ArrayList<Entry<String, Integer>> distinctUrlCounts = driver.getBulkDistinctDomainObjectCount(
                        campaignId, skip, limit, DirenajObjects.Url);

                retHtmlStr += tester.testFreqURL(distinctUrlCounts);

            } else if (operation.equals("getUserCentralities")) {
                Map<CentralityTypes, ArrayList<Entry<User, BigDecimal>>> centralitiesOfUsers = CentralityAnalysis
                        .calculateCentralityOfUsers(userId, password, campaignId, skip, limit);

                retHtmlStr += tester.testCentrality(centralitiesOfUsers);
            } else if (operation.equals("getHashtagTimeline")) {
                request.setAttribute("campaignId", campaignId);
                request.setAttribute("operation", operation);
                request.setAttribute("limit", limit);
                counts = driver.countHastags(campaignId, skip, limit);
                request.setAttribute("hashtagCounts", counts);
                ServletContext context = getServletContext();
                RequestDispatcher dispatcher = context.getRequestDispatcher("/hashtagTimeLineRequest.jsp");
                dispatcher.forward(request, response);
            } else if (operation.equals("findCommunities")) {
                List<Relations> relations = new Vector<Relations>();
                String[] checkBoxValues = request.getParameterValues("graphRelation");
                for (int i = 0; i < checkBoxValues.length; i++) {
                    relations.add(Relations.valueOf(checkBoxValues[i]));
                }
                String modularityValue = request.getParameter("modularityValue");
                // get user graphs
                DirenajGraph<User> userRelationsGraph = GraphUtil.formUserRelationsGraph(userId, password, campaignId,
                        skip, limit, relations, null);
                DetectedCommunities communitiesInCampaign = CommunityDetector.getCommunitiesInCampaign(
                        Double.valueOf(modularityValue).doubleValue(), userRelationsGraph, false);
                request.setAttribute("detectedCommunities", communitiesInCampaign);
                request.setAttribute("campaignId", campaignId);
                request.setAttribute("limit", limit);
                ServletContext context = getServletContext();
                RequestDispatcher dispatcher = context.getRequestDispatcher("/communityDisplay.jsp");
                dispatcher.forward(request, response);
            } else if (operation.equals("showCommunities")) {
                // get relations
                List<Relations> relations = new Vector<Relations>();
                String[] checkBoxValues = request.getParameterValues("graphRelation");
                for (int i = 0; i < checkBoxValues.length; i++) {
                    relations.add(Relations.valueOf(checkBoxValues[i]));
                }
                session.setAttribute("graphRelation", relations);
                // get modularity
                String modularityValue = request.getParameter("modularityValue");
                session.setAttribute("modularityValue", modularityValue);
                // get d3 Lib Format
                String d3LibFormatValue = request.getParameter("d3LibFormat");
                D3GraphicType d3GraphicType = D3GraphicType.valueOf(d3LibFormatValue);
                session.setAttribute("d3LibFormat", d3GraphicType);

                String classificationMethod = request.getParameter("classificationMethod");
                if (!TextUtils.isEmpty(classificationMethod)) {
                    session.setAttribute("classificationMethod", classificationMethod);
                } else {
                    session.removeAttribute("classificationMethod");
                }

                // forward to jsp
                ServletContext context = getServletContext();
                RequestDispatcher dispatcher = null;
                switch (d3GraphicType) {
                case HierarchicalEdgeBundle:
                    dispatcher = context.getRequestDispatcher("/hierarchicalEdgeBundle.jsp");
                    break;
                case CirclePack:
                    dispatcher = context.getRequestDispatcher("/zoomableCommunityCirclePack.jsp");
                    break;
                case ForceDirectedGraph:
                    dispatcher = context.getRequestDispatcher("/forceDirectedGraph.jsp");
                    break;
                }
                dispatcher.forward(request, response);

            } else if (operation.equals("getAllUsers")) {

                // create Weights object
                BotAccountCalculationWeight botAccountCalculationWeight = new BotAccountCalculationWeight();
                botAccountCalculationWeight.setCreationDateWeight(new BigDecimal(request
                        .getParameter("creationDateWeight")));
                botAccountCalculationWeight.setFriendFollowerWeight(new BigDecimal(request
                        .getParameter("friendFollowerWeight")));
                botAccountCalculationWeight.setHashtagRatioWeight(new BigDecimal(request
                        .getParameter("hashtagRatioWeight")));
                botAccountCalculationWeight.setMentionRatioWeight(new BigDecimal(request
                        .getParameter("mentionRatioWeight")));
                botAccountCalculationWeight.setProtectedAccountWeight(new BigDecimal(request
                        .getParameter("protectedAccountWeight")));
                botAccountCalculationWeight.setUrlRatioWeight(new BigDecimal(request.getParameter("urlRatioWeight")));
                botAccountCalculationWeight.setVerifiedAccountWeight(new BigDecimal(request
                        .getParameter("verifiedAccountWeight")));

                ArrayList<User> bulkUsersInTweets = driver.getBulkUsersInTweets(campaignId, skip, limit);
                UserAccountPropertyAnalyser.analyseUserAccountProperties(bulkUsersInTweets);
                Hashtable<User, BigDecimal> userProbabilities = UserAccountPropertyAnalyser
                        .calculateProbabilityOfBeingHuman(bulkUsersInTweets, botAccountCalculationWeight);
                ArrayList<Entry<User, BigDecimal>> sortedValues = CollectionUtil.sortValues(userProbabilities,false);

                request.setAttribute("sortedValues", sortedValues);
                ServletContext context = getServletContext();
                RequestDispatcher dispatcher = context.getRequestDispatcher("/botUserAnalysis.jsp");
                dispatcher.forward(request, response);
            } else {
                retHtmlStr += "OPERATION NOT SUPPORTED!";
            }

        } catch (Exception e) {
            out.println(e.getMessage());
            out.close();
            System.out.println(e);
        }

        out.println(retHtmlStr + "</body></html>");
        out.close();
    }
}

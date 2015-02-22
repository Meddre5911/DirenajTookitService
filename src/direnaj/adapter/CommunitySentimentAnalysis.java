package direnaj.adapter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;

import direnaj.domain.Community;
import direnaj.domain.User;
import direnaj.functionalities.classification.ClassificationMethods;
import direnaj.functionalities.graph.DirenajGraph;
import direnaj.functionalities.graph.GraphUtil;
import direnaj.functionalities.graph.Relations;
import direnaj.functionalities.sentiment.SentimentDetector;
import direnaj.functionalities.sna.communityDetection.CommunityDetector;
import direnaj.functionalities.sna.communityDetection.DetectedCommunities;
import direnaj.util.d3Lib.D3GraphicType;

public class CommunitySentimentAnalysis extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = req.getSession();
        D3GraphicType d3GraphicType = (D3GraphicType) session.getAttribute("d3LibFormat");
        DetectedCommunities detectedCommunities = (DetectedCommunities) session.getAttribute("detectedCommunities");
        DetectedCommunities sentimentalCommunity = (DetectedCommunities) session.getAttribute("sentimentalCommunity");

        Boolean isSentimentCommunityRequested = Boolean.valueOf(req.getParameter("sentimentalCommunity"));

        String communityJSon;
        try {
            if (isSentimentCommunityRequested) {
                communityJSon = sentimentalCommunity.getJsonOfCommunities(d3GraphicType);
            } else {
                communityJSon = detectedCommunities.getJsonOfCommunities(d3GraphicType);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter printout = response.getWriter();
        printout.println(communityJSon);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String userId = (String) session.getAttribute("userID");
        String password = (String) session.getAttribute("pass");
        String campaignId = (String) session.getAttribute("campaignID");
        int skip = (Integer) session.getAttribute("skip");
        int limit = (Integer) session.getAttribute("limit");

        List<Relations> relations = (List<Relations>) session.getAttribute("graphRelation");
        D3GraphicType d3GraphicType = (D3GraphicType) session.getAttribute("d3LibFormat");
        double modularityValue = Double.valueOf((String) session.getAttribute("modularityValue"));

        String searchedCommunity = req.getParameter("searchedCommunity");
        String classificationMethod = req.getParameter("classificationMethod");
        ClassificationMethods selectedClassification = ClassificationMethods.valueOf(classificationMethod);
        req.setAttribute("searchedCommunityName", searchedCommunity);

        // get non sentiment communities
        DetectedCommunities detectedCommunities = (DetectedCommunities) session.getAttribute("detectedCommunities");
        Vector<Community> allCommunities = detectedCommunities.getDetectedCommunties(true);
        Vector<User> usersInCommunity = null;
        DirenajGraph<User> userGraph = null;
        Vector<Community> communities2Remove = new Vector<Community>();
        for (Community community : allCommunities) {
            if (community.getCommunityName().equalsIgnoreCase(searchedCommunity)) {
                usersInCommunity = community.getUsersInCommunity();
                userGraph = community.getUserGraph();
            } else {
                communities2Remove.add(community);
            }
        }
        detectedCommunities.removeCommunities(communities2Remove);
        userGraph.removeOtherVerticesfromGraph(usersInCommunity);
        session.setAttribute("detectedCommunities", detectedCommunities);

        // apply sentiment
        // get user graphs
        SentimentDetector sentimentDetector = new SentimentDetector(selectedClassification);
        DirenajGraph<User> userSentimentalRelationsGraph = GraphUtil.formUserRelationsGraph(userId, password,
                campaignId, skip, limit, relations, sentimentDetector);
        GraphUtil.analyzeSentimentalGraph(userSentimentalRelationsGraph);
        userSentimentalRelationsGraph.removeOtherVerticesfromGraph(usersInCommunity);
        DetectedCommunities communityWithSentiments = CommunityDetector.getCommunitiesInCampaign(modularityValue,
                userSentimentalRelationsGraph, true);
        session.setAttribute("sentimentalCommunity", communityWithSentiments);

        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher("/communitySentimentResult.jsp");
        dispatcher.forward(req, resp);

    }

}

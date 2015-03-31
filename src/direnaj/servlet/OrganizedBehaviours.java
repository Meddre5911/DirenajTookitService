package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import direnaj.functionalities.OrganizationDetector;
import direnaj.util.TextUtils;

public class OrganizedBehaviours extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // getting parameters from html form
        Map<String, String[]> params = request.getParameterMap();

        String userId = params.get("userID")[0];
        String password = params.get("pass")[0];

        String operationType = params.get("operationType")[0];

        System.out.println(operationType);
        int topHashTagCount = TextUtils.getIntegerValue(request.getParameter("topHashtagCount"));
        String organizedHashtagDefinition = request.getParameter("organizedHashtagDefinition");
        String campaignId = request.getParameter("campaignId");
        String limit = request.getParameter("limit");
        String skip = request.getParameter("skip");

        OrganizationDetector organizationDetector = new OrganizationDetector(userId, password, campaignId,
                topHashTagCount, organizedHashtagDefinition, skip, limit, "");
        organizationDetector.detectOrganizedBehaviourInHashtags();

    }

}

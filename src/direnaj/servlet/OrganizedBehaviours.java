package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import direnaj.functionalities.OrganizationDetector;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class OrganizedBehaviours extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, String[]> params = request.getParameterMap();
        String userId = params.get("userID")[0];
        String password = params.get("pass")[0];
        String operationType = params.get("operationType")[0];
        String campaignId = request.getParameter("campaignId");
        System.out.println(operationType);

        // getting parameters from html form
        String retHtmlStr = "<!DOCTYPE html>\n" + "<html>\n" + "<head><title>Direnaj Test Center</title></head>\n"
                + "<body bgcolor=\"#fdf5e6\">\n" + "<h1>DirenajDriver Test</h1>\n" + "<p>CampaignID : <b>" + campaignId
                + "</b> | Operation : <b>" + operationType + "</b> </p><hr>\n";

        try {
            String realUserId = PropertiesUtil.getInstance().getProperty("direnajUserId");
            String realPassword = PropertiesUtil.getInstance().getProperty("direnajPassword");
            if (!realUserId.equals(userId) || !realPassword.equals(password)) {
                retHtmlStr += "Wrong UserName - Password <br>";
            } else {
                int topHashTagCount = TextUtils.getIntegerValue(request.getParameter("topHashtagCount"));
                String tracedHashtag = TextUtils.getNotNullValue(request.getParameter("tracedHashtag"));
                String organizedHashtagDefinition = request.getParameter("organizedHashtagDefinition");
                OrganizationDetector organizationDetector = new OrganizationDetector(campaignId, topHashTagCount,
                        organizedHashtagDefinition, tracedHashtag,
                        OrganizedBehaviourDetectionRequestType.valueOf(operationType));
                new Thread(organizationDetector).start();
                forwardRequest(request, response, "/listOrganizedBehaviourRequests.jsp");
                return;
            }
            out.println(retHtmlStr + "</body></html>");
        } catch (Exception e) {
            out.println(retHtmlStr + "</body></html>");
        } finally {
            out.close();
        }

    }

    private void forwardRequest(HttpServletRequest request, HttpServletResponse response, String jspName)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        RequestDispatcher dispatcher = context.getRequestDispatcher(jspName);
        dispatcher.forward(request, response);
    }

}

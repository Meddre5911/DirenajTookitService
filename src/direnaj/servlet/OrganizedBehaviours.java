package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import direnaj.functionalities.organizedBehaviour.OrganizationDetector;
import direnaj.util.DateTimeUtils;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class OrganizedBehaviours extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		OrganizationDetector organizationDetector = null;
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		String retHtmlStr = "";
		try {
			String actionType = TextUtils.getNotNullValue(request.getParameter("actionType"));
			// actionType=resume&requestId='+data+'
			if ("resume".equals(actionType)) {
				String requestId = request.getParameter("requestId");
				organizationDetector = new OrganizationDetector(requestId);
				new Thread(organizationDetector).start();
				forwardRequest(request, response, "/listOrganizedBehaviourRequests.jsp");
				return;
			} else {
				Map<String, String[]> params = request.getParameterMap();
				String userId = params.get("userID")[0];
				String password = params.get("pass")[0];
				String operationType = params.get("operationType")[0];
				String campaignId = request.getParameter("campaignId");

				String workUntilBreakPoint = null;
				if (params.get("resumeBreakPoint") != null && params.get("resumeBreakPoint").length > 0) {
					workUntilBreakPoint = params.get("resumeBreakPoint")[0];
				}
				Logger.getLogger(OrganizedBehaviours.class).debug("Operation type : " + operationType);

				// getting parameters from html form
				retHtmlStr = "<!DOCTYPE html>\n" + "<html>\n" + "<head><title>Direnaj Test Center</title></head>\n"
						+ "<body bgcolor=\"#fdf5e6\">\n" + "<h1>DirenajDriver Test</h1>\n" + "<p>CampaignID : <b>"
						+ campaignId + "</b> | Operation : <b>" + operationType + "</b> </p><hr>\n";

				String realUserId = PropertiesUtil.getInstance().getProperty("direnajUserId", null);
				String realPassword = PropertiesUtil.getInstance().getProperty("direnajPassword", null);
				if (!realUserId.equals(userId) || !realPassword.equals(password)) {
					retHtmlStr += "Wrong UserName - Password <br>";
				} else {
					int topHashTagCount = 1;
					String tracedHashtag = TextUtils.getNotNullValue(request.getParameter("tracedHashtag"));
					String organizedHashtagDefinition = request.getParameter("organizedHashtagDefinition");
					boolean disableGraphAnalysis = !TextUtils.isEmpty(request.getParameter("disableGraphDb"));
					boolean calculateHashTagSimilarity = !TextUtils
							.isEmpty(request.getParameter("calculateHashTagSimilarity"));
					boolean calculateGeneralSimilarity = !TextUtils
							.isEmpty(request.getParameter("calculateGeneralSimilarity"));
					boolean bypassTweetCollection = !TextUtils.isEmpty(request.getParameter("bypassTweetCollection"));

					String latestDateStr = request.getParameter("latestDate");
					String earliestDateStr = request.getParameter("earliestDate");
					Date latestDate = null;
					Date earliestDate = null;
					boolean isExternalDateGiven = false;
					
					if (!TextUtils.isEmpty(latestDateStr) && !TextUtils.isEmpty(earliestDateStr)) {
						try{
							latestDate = DateTimeUtils.getUTCDateTimeWithFormat("yyyy-MM-dd hh:mm", latestDateStr);
							earliestDate = DateTimeUtils.getUTCDateTimeWithFormat("yyyy-MM-dd hh:mm", earliestDateStr);
							isExternalDateGiven = true;
						}catch(Exception e){
							 latestDate = null;
							 earliestDate = null;
						}
					} 

					organizationDetector = new OrganizationDetector(campaignId, topHashTagCount,
							organizedHashtagDefinition, tracedHashtag,
							OrganizedBehaviourDetectionRequestType.valueOf(operationType), disableGraphAnalysis,
							calculateHashTagSimilarity, calculateGeneralSimilarity, bypassTweetCollection,
							workUntilBreakPoint,isExternalDateGiven, earliestDate, latestDate);

					new Thread(organizationDetector).start();
					forwardRequest(request, response, "/listOrganizedBehaviourRequests.jsp");
					return;
				}
				out.println(retHtmlStr + "</body></html>");
			}
		} catch (Exception e) {
			e.printStackTrace();
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

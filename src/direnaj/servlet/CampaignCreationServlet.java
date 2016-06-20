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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import direnaj.driver.DirenajMongoDriver;
import direnaj.driver.MongoCollectionFieldNames;
import direnaj.functionalities.organizedBehaviour.CampaignCreator;
import direnaj.util.PropertiesUtil;

public class CampaignCreationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		String retHtmlStr = "";
		try {
			Map<String, String[]> params = request.getParameterMap();
			String userId = params.get("userID")[0];
			String password = params.get("pass")[0];
			String campaignId = request.getParameter("campaignId");
			String campaignDefinition = params.get("campaignDefinition")[0];
			String requestedHashtags = params.get("requestedHashtags")[0];
			String minDateStr = params.get("minDate")[0];
			String maxDateStr = params.get("maxDate")[0];

			// getting parameters from html form
			retHtmlStr = "<!DOCTYPE html>\n" + "<html>\n" + "<head><title>Direnaj Test Center</title></head>\n"
					+ "<body bgcolor=\"#fdf5e6\">\n" + "<h1>DirenajDriver Test</h1>\n" + "<p>CampaignID : <b>"
					+ campaignId + "</b> | Operation : <b>CampaignCreation</b> </p><hr>\n";

			String realUserId = PropertiesUtil.getInstance().getProperty("direnajUserId", null);
			String realPassword = PropertiesUtil.getInstance().getProperty("direnajPassword", null);
			if (!realUserId.equals(userId) || !realPassword.equals(password)) {
				retHtmlStr += "Wrong UserName - Password <br>";
			} else {
				BasicDBObject query = new BasicDBObject(MongoCollectionFieldNames.MONGO_CAMPAIGN_ID, campaignId);
				DBObject existedCampaign = DirenajMongoDriver.getInstance().getCampaignsCollection().findOne(query);
				if (existedCampaign != null) {
					retHtmlStr += "Existed Campaign. Please Try Different Name.";
				} else {
					CampaignCreator campaignCreator = new CampaignCreator(campaignId, campaignDefinition,
							requestedHashtags, minDateStr, maxDateStr);
					new Thread(campaignCreator).start();
					forwardRequest(request, response, "/listToolkitCampaigns.jsp");
					return;
				}
			}
			out.println(retHtmlStr + "</body></html>");
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

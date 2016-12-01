package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import direnaj.functionalities.organizedBehaviour.CampaignComparer;
import direnaj.util.PropertiesUtil;
import direnaj.util.TextUtils;

public class CampaignComparisonServlet extends HttpServlet {

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

			// getting parameters from html form
			retHtmlStr = "<!DOCTYPE html>\n" + "<html>\n" + "<head><title>Direnaj Test Center</title></head>\n"
					+ "<body bgcolor=\"#fdf5e6\">\n"
					+ "<h1>DirenajDriver Test</h1>\n Operation : <b>Campaign Comparison</b> </p><hr>\n";

			String realUserId = PropertiesUtil.getInstance().getProperty("direnajUserId", null);
			String realPassword = PropertiesUtil.getInstance().getProperty("direnajPassword", null);
			if (!realUserId.equals(userId) || !realPassword.equals(password)) {
				retHtmlStr += "Wrong UserName - Password <br>";
			} else {

				String actualCampaignId = request.getParameter("actualCampaignId");
				String actualHashtag = request.getParameter("actualHashtag");
				String requestDefinition = request.getParameter("requestDefinition");

				Map<String, String> comparisonCampaignHashtagInfo = new HashMap<>();
				for (int i = 1; i <= 10; i++) {
					String comparedCampaign = request.getParameter("comparedCampaignId" + i);
					String comparedHashtag = request.getParameter("comparedHashtag" + i);
					if (!TextUtils.isEmpty(comparedCampaign)) {
						comparisonCampaignHashtagInfo.put(comparedCampaign, comparedHashtag);
					}
				}

				if (TextUtils.isEmpty(actualCampaignId) || comparisonCampaignHashtagInfo.isEmpty()) {
					throw new Exception();
				}

				CampaignComparer campaignComparer = new CampaignComparer(actualCampaignId, actualHashtag,
						comparisonCampaignHashtagInfo, requestDefinition);
				new Thread(campaignComparer).start();
				forwardRequest(request, response, "/listCampaignComparisons.jsp");
				return;
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

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

import direnaj.functionalities.organizedBehaviour.OrganizedBehaviourFeatureExtractor;
import direnaj.util.PropertiesUtil;

public class OrganizedBehavioursExtractFeaturesServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
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
						+ "<body bgcolor=\"#fdf5e6\">\n" + "<h1>DirenajDriver Test</h1>\n" + "<p>Extract Feature Request </p><hr>\n";

				String realUserId = PropertiesUtil.getInstance().getProperty("direnajUserId", null);
				String realPassword = PropertiesUtil.getInstance().getProperty("direnajPassword", null);
				if (!realUserId.equals(userId) || !realPassword.equals(password)) {
					retHtmlStr += "Wrong UserName - Password <br>";
				} else {

					String[] featureExtractionIds = params.get("willBeExtracted");
					OrganizedBehaviourFeatureExtractor featureExtractor = new OrganizedBehaviourFeatureExtractor(featureExtractionIds);
					new Thread(featureExtractor).start();
				}
				retHtmlStr += "<br><b>Feature Extraction Process started to Run</b>";
				out.println(retHtmlStr + "</body></html>");
				
		} catch (Exception e) {
			e.printStackTrace();
			retHtmlStr += e.getMessage();
			out.println(retHtmlStr + "</body></html>");
		} finally {
			out.close();
		}
		return;
	}

	private void forwardRequest(HttpServletRequest request, HttpServletResponse response, String jspName)
			throws ServletException, IOException {
		ServletContext context = getServletContext();
		RequestDispatcher dispatcher = context.getRequestDispatcher(jspName);
		dispatcher.forward(request, response);
	}

}

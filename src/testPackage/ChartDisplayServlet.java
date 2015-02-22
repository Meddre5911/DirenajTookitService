package testPackage;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.joda.time.DateTime;

import direnaj.functionalities.sna.AnalysisIntervals;
import direnaj.functionalities.sna.HashtagAnalysis;

public class ChartDisplayServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        // get session params
        HashMap<DateTime, Integer> timelyIntervalAnalysis = (HashMap<DateTime, Integer>) session
                .getAttribute("hashTagTimelyIntervalAnalysis");
        // get req paramaters
        String searchedHashtag = request.getParameter("searchedHashtagName");
        String timeInterval = request.getParameter("timeInterval");
        // get jChart
        JFreeChart chartOfHashtags = HashtagAnalysis.getGraphicsOfHashtags(timelyIntervalAnalysis,
                AnalysisIntervals.valueOf(timeInterval), searchedHashtag);
        // push chart to OutputStream
        OutputStream out = response.getOutputStream(); /* Get the output stream from the response object */
        response.setContentType("image/png"); /* Set the HTTP Response Type */
        ChartUtilities.writeChartAsJPEG(out, chartOfHashtags, 400, 300);

    }

}

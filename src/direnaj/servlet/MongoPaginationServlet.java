package direnaj.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import direnaj.driver.DirenajMongoDriver;

public class MongoPaginationServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        String pageType = request.getParameter("pageType");
        JSONArray jsonArray = new JSONArray();
        JSONObject responseJSonObject = new JSONObject();

        HttpSession session = request.getSession(true);

        Integer pageDisplayLength = Integer.valueOf(request.getParameter("iDisplayLength"));
        //Fetch the page number from client
        Integer pageNumber = (Integer.valueOf(request.getParameter("iDisplayStart")) / pageDisplayLength);
        String sEcho = request.getParameter("sEcho");
        int recordCounts = 0;

        try {
            if ("requestListPagination".equals(pageType)) {
                // mongo call
                DBCollection orgBehaviorRequestCollection = DirenajMongoDriver.getInstance()
                        .getOrgBehaviorRequestCollection();
                if (session.getAttribute("collectionRecordCount") == null) {
                    recordCounts = (int) orgBehaviorRequestCollection.count();
                    session.setAttribute("collectionRecordCount", recordCounts);
                } else {
                    recordCounts = Integer.valueOf(session.getAttribute("collectionRecordCount").toString());
                }
                // get cursor
                DBCursor paginatedResult = orgBehaviorRequestCollection.find().sort(new BasicDBObject("_id", -1))
                        .skip(pageNumber * pageDisplayLength).limit(pageDisplayLength);
                // get objects from cursor
                try {
                    while (paginatedResult.hasNext()) {
                        jsonArray.put(new JSONObject(paginatedResult.next().toString()));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else if ("requestInputData".equals(pageType)) {
                String requestId = request.getParameter("retrievedRequestId");
                DBCollection inputDataCollection = DirenajMongoDriver.getInstance().getOrgBehaviourProcessInputData();
                BasicDBObject query = new BasicDBObject("requestId", requestId);

                if (session.getAttribute("userInputDataCount") == null) {
                    recordCounts = (int) inputDataCollection.count(query);
                    session.setAttribute("userInputDataCount", recordCounts);
                } else {
                    recordCounts = Integer.valueOf(session.getAttribute("userInputDataCount").toString());
                }
                // get cursor
                DBCursor paginatedResult = inputDataCollection.find(query).skip(pageNumber * pageDisplayLength)
                        .limit(pageDisplayLength);
                // get objects from cursor
                try {
                    while (paginatedResult.hasNext()) {
                        jsonArray.put(new JSONObject(paginatedResult.next().toString()));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            responseJSonObject.put("iTotalRecords", recordCounts);
            responseJSonObject.put("iTotalDisplayRecords", recordCounts);
            responseJSonObject.put("sEcho", sEcho);
            responseJSonObject.put("aaData", jsonArray);
            out.print(responseJSonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().print(e.getMessage());
        }
    }

}

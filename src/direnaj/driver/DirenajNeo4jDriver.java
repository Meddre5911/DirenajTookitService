package direnaj.driver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import direnaj.util.PropertiesUtil;

public class DirenajNeo4jDriver {

    private static DirenajNeo4jDriver direnajNeo4jDriver;
    private String serverRootUri;

    private DirenajNeo4jDriver() {
        serverRootUri = PropertiesUtil.getInstance().getProperty("neo4j.server.rootUri", null);
    }

    public static DirenajNeo4jDriver getInstance() {
        if (direnajNeo4jDriver == null) {
            direnajNeo4jDriver = new DirenajNeo4jDriver();
        }
        return direnajNeo4jDriver;
    }

    public void startService() {
        serverRootUri = PropertiesUtil.getInstance().getProperty("neo4j.server.rootUri", null);
        WebResource resource = Client.create().resource(serverRootUri);
        ClientResponse response = resource.get(ClientResponse.class);
        Logger.getLogger(DirenajNeo4jDriver.class).debug(
                String.format("GET on [%s], status code [%d]", serverRootUri, response.getStatus()));
        response.close();
    }

    public void executeCypher() {
        final String txUri = serverRootUri + "transaction/commit";
        WebResource resource = Client.create().resource(txUri);

        String query = "Match (t:JAVA_SERVER_CALL_DENEME_erdem {id: 1}) RETURN t";

        String payload = "{\"statements\" : [ {\"statement\" : \"" + query + "\"} ]}";
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .entity(payload).post(ClientResponse.class);

        String responseEntity = response.getEntity(String.class);
        Logger.getLogger(DirenajNeo4jDriver.class).info(
                String.format(
                        "POST [%s] to [%s], status code [%d], returned data: " + System.getProperty("line.separator")
                                + "%s", payload, txUri, response.getStatus(), responseEntity));

        Logger.getLogger(DirenajNeo4jDriver.class).info("\n response entitiy : \n" + responseEntity);

        try {
            JSONObject jsonObject = new JSONObject(responseEntity);
            JSONObject resultObject = jsonObject.getJSONArray("results").getJSONObject(0);
            JSONArray dataArray = resultObject.getJSONArray("data");
            Object result = dataArray.getJSONObject(0).getJSONArray("row").getJSONObject(0).get("id");
            Logger.getLogger(DirenajNeo4jDriver.class).info("Result : " + Integer.valueOf(result.toString()));

        } catch (JSONException e) {
            Logger.getLogger(DirenajNeo4jDriver.class).error("Error in DirenajNeo4jDriver.", e);
        }
        //        {
        //            "results": [
        //              {
        //                "columns": [
        //                  "t"
        //                ],
        //                "data": [
        //                  {
        //                    "row": [
        //                      {
        //                        "id": 1
        //                      }
        //                    ]
        //                  }
        //                ]
        //              }
        //            ],
        //            "errors": [
        //              
        //            ]
        //          }
        response.close();
    }

    public void executeNoResultCypher(String cypherQuery, String params) {
        Logger.getLogger(DirenajNeo4jDriver.class).info("Cypher Query");
        Logger.getLogger(DirenajNeo4jDriver.class).info(cypherQuery);
        final String txUri = serverRootUri + "transaction/commit";
        WebResource resource = Client.create().resource(txUri);
        String payload = "{\"statements\" : [ {\"statement\" : \"" + cypherQuery + "\",\"parameters\": " + params
                + "}]}";
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .entity(payload).post(ClientResponse.class);
        Logger.getLogger(DirenajNeo4jDriver.class).info(
                String.format(
                        "POST [%s] to [%s], status code [%d], returned data: " + System.getProperty("line.separator")
                                + "%s", payload, txUri, response.getStatus(), response.getEntity(String.class)));
        response.close();
    }

    public Map<String, Object> executeSingleResultCypher(String cypherQuery, String queryParams, List<String> keyStrings) {
        final String txUri = serverRootUri + "transaction/commit";
        Map<String, Object> cypherQueryResult = new HashMap<>();
        WebResource resource = Client.create().resource(txUri);
        String payload = "{\"statements\" : [ {\"statement\" : \"" + cypherQuery + "\",\"parameters\": " + queryParams
                + "} ]}";
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON)
                .entity(payload).post(ClientResponse.class);
        String responseEntity = response.getEntity(String.class);
        Logger.getLogger(DirenajNeo4jDriver.class).info(
                String.format(
                        "POST [%s] to [%s], status code [%d], returned data: " + System.getProperty("line.separator")
                                + "%s", payload, txUri, response.getStatus(), responseEntity));
        try {
            Logger.getLogger(DirenajNeo4jDriver.class).info("Response Entity : " + responseEntity);
            JSONObject jsonObject = new JSONObject(responseEntity);
            JSONObject resultObject = jsonObject.getJSONArray("results").getJSONObject(0);
            JSONArray dataArray = resultObject.getJSONArray("data");
            Object result = dataArray.getJSONObject(0).getJSONArray("row").get(0);
            for (String key : keyStrings) {
                cypherQueryResult.put(key, result);
            }
        } catch (JSONException e) {
            Logger.getLogger(DirenajNeo4jDriver.class).error("Error in executeSingleResultCypher", e);
        }
        response.close();
        return cypherQueryResult;
    }

    public static void main(String[] args) {
        DirenajNeo4jDriver.getInstance().startService();
        DirenajNeo4jDriver.getInstance().executeCypher();
    }
}

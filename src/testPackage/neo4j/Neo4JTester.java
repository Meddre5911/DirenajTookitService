package testPackage.neo4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import direnaj.driver.DirenajNeo4jDriver;
import direnaj.functionalities.OrganizationDetector;
import direnaj.servlet.OrganizedBehaviourDetectionRequestType;
import direnaj.util.ListUtils;

public class Neo4JTester {
    public static void main(String[] args) {
        OrganizationDetector detector = new OrganizationDetector("erdem_deneme_8", 5, "Neo4JDeneme", "",
                OrganizedBehaviourDetectionRequestType.CheckHashtagsInCampaign);
        List<String> userIds = ListUtils.getListOfStrings("1195");
        String subgraphEdgeLabel = detector.createSubgraphByAddingEdges(userIds);
        System.out.println("SubGraph Label : " + subgraphEdgeLabel);
        
        
        
//       DirenajNeo4jDriver.getInstance().executeNoResultCypher("match (u:User{id_str:\"1632442946\"}) return u;", "");
        
       HashMap<String, Double> userClosenessCentralities = detector.calculateInNeo4J(userIds, subgraphEdgeLabel);
        Set<Entry<String, Double>> entrySet = userClosenessCentralities.entrySet();
        for (Entry<String, Double> entry : entrySet) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    
    }

}

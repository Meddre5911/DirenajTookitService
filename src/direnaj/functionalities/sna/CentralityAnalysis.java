package direnaj.functionalities.sna;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import direnaj.domain.User;
import direnaj.functionalities.graph.DirenajGraph;
import direnaj.functionalities.graph.GraphUtil;
import direnaj.functionalities.graph.Relations;
import direnaj.util.CollectionUtil;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;

public class CentralityAnalysis {

    public static Map<CentralityTypes, ArrayList<Entry<User, BigDecimal>>> calculateCentralityOfUsers(String direnajId,
            String pass, String campaignID, int skip, int limit) throws Exception {
        // initialize Result vectors
        Hashtable<User, BigDecimal> betweennessCentralityResults = new Hashtable<User, BigDecimal>();
        Hashtable<User, BigDecimal> degreeCentralityResults = new Hashtable<User, BigDecimal>();
        DirenajGraph<User> userRelationsGraph = GraphUtil.formUserRelationsGraph(direnajId, pass, campaignID, skip,
                limit, new Vector<Relations>(), null);

        BetweennessCentrality<User, String> betweennessCentrality = new BetweennessCentrality<User, String>(
                userRelationsGraph.getJungGraph(), true, false);
        betweennessCentrality.setRemoveRankScoresOnFinalize(false);
        betweennessCentrality.evaluate();
        // List<Ranking<?>> rankings = betweennessCentrality.getRankings();

        Collection<User> allUsers = userRelationsGraph.getVertices();
        for (Iterator<User> iterator = allUsers.iterator(); iterator.hasNext();) {
            User userNode = (User) iterator.next();
            // closenessCentralityResults.put(String.valueOf(vertex),
            // closenessCentrality.getVertexScore(vertex));
            betweennessCentralityResults.put(userNode,
                    BigDecimal.valueOf(betweennessCentrality.getVertexRankScore(userNode)));
            degreeCentralityResults.put(userNode, BigDecimal.valueOf(userRelationsGraph.getInDegree(userNode)));
        }
        ArrayList<Entry<User, BigDecimal>> betweennessResults = CollectionUtil.sortValues(betweennessCentralityResults);
        ArrayList<Entry<User, BigDecimal>> degreeResults = CollectionUtil.sortValues(degreeCentralityResults);

        Map<CentralityTypes, ArrayList<Entry<User, BigDecimal>>> centralities = new HashMap<CentralityTypes, ArrayList<Entry<User, BigDecimal>>>();
        centralities.put(CentralityTypes.Betweenness, betweennessResults);
        centralities.put(CentralityTypes.InDegree, degreeResults);
        return centralities;

        //        System.out.println("------------- Betweenness Centrality ----------");
        //        for (Entry<User, BigDecimal> entry : betweennessResults) {
        //            if (entry.getValue().compareTo(BigDecimal.ZERO) > 1) {
        //                System.out.println("User : " + entry.getKey().getUserScreenName() + " - " + entry.getValue());
        //            }
        //        }
        //        System.out.println("\n------------- Degree Centrality ----------");
        //        for (Entry<User, BigDecimal> entry : degreeResults) {
        //            if (entry.getValue().compareTo(BigDecimal.ZERO) > 1) {
        //                System.out.println("User : " + entry.getKey().getUserScreenName() + " - " + entry.getValue());
        //            }
        //        }
        //        userRelationsGraph.visualizeGraph();
    }

}

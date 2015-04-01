package direnaj.driver;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class DirenajNeo4jDriver {
    public static String NEO4J_DB_PATH = "/home/erdem/Documents/neo4j-enterprise-2.1.6/data/graph.db";

    private GraphDatabaseService neo4jService;
    private static DirenajNeo4jDriver direnajNeo4jDriver;

    private DirenajNeo4jDriver() {
        neo4jService = new GraphDatabaseFactory().newEmbeddedDatabase(NEO4J_DB_PATH);
        registerShutdownHook(neo4jService);

    }

    public static DirenajNeo4jDriver getInstance() {
        if (direnajNeo4jDriver == null) {
            direnajNeo4jDriver = new DirenajNeo4jDriver();
        }
        return direnajNeo4jDriver;
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    public void setNeo4jService(GraphDatabaseService neo4jService) {
        this.neo4jService = neo4jService;
    }

    public GraphDatabaseService getNeo4jService() {
        return neo4jService;
    }

    public void executeNoResultCypher(String cypherQuery, Map<String, Object> map) {
        Transaction tx = neo4jService.beginTx();
        try {
            if (map == null) {
                neo4jService.execute(cypherQuery);
            } else {
                neo4jService.execute(cypherQuery, map);
            }
            tx.success();
        } catch (Exception e) {
            tx.failure();
            // FIXME log yaz
            System.out.println("Neo4j Failed Query : " + cypherQuery);
            e.printStackTrace();
        } finally {
            tx.close();
        }
    }
}

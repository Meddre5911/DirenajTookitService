package direnaj.driver;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class DirenajNeo4jDriver {
    public static String NEO4J_DB_PATH = "/home/erdem/Documents/neo4j-enterprise-2.1.6/data/graph.db";

    @SuppressWarnings("unused")
    private GraphDatabaseService neo4jService;
    private static DirenajNeo4jDriver direnajNeo4jDriver;

    private DirenajNeo4jDriver() {
        neo4jService = new GraphDatabaseFactory().newEmbeddedDatabase(NEO4J_DB_PATH);
        registerShutdownHook(getNeo4jService());

    }

    @SuppressWarnings("static-access")
    public static GraphDatabaseService getNeo4jService() {
        if (direnajNeo4jDriver == null) {
            direnajNeo4jDriver = new DirenajNeo4jDriver();
        }
        return direnajNeo4jDriver.neo4jService;
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
}

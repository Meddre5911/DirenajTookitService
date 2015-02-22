package direnaj.functionalities.graph;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JFrame;

import direnaj.domain.User;
import direnaj.functionalities.sentiment.Sentiment;
import direnaj.util.DateTimeUtils;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class DirenajGraph<T> {

    private Graph<T, String> graph;
    private Integer edgeCount = null;
    private TreeMap<T, Vector<String>> vertexObjectMapping;

    /**
     * FIXME istenilen Graph yapýsýna göre, burada parametrik olarak farklý graphlar yaratýlabilir
     */
    public DirenajGraph() {
        graph = new SparseMultigraph<T, String>();
        vertexObjectMapping = new TreeMap<T, Vector<String>>();
    }

    public void addEdge2Graph(T node, T node2, Relations relation, Sentiment sentiment) {
        if (node != null && node2 != null) {
            graph.addEdge(sentiment.name() + "-" + relation + "-" + node.toString() + "-" + node2.toString() + "-"
                    + DateTimeUtils.getLocalDate().getTime() + Math.random(), node, node2, EdgeType.DIRECTED);
        }
    }

    public void add2Graph(T node) {
        // add user to graph
        if (node != null && !graph.containsVertex(node)) {
            graph.addVertex(node);
        }
    }

    public Graph<T, String> getJungGraph() {
        return graph;
    }

    public Collection<T> getVertices() {
        return graph.getVertices();
    }

    public Double getInDegree(T node) {
        return (double) graph.inDegree(node);
    }

    public Double getVertexDegree(T node) {
        return (double) graph.degree(node);
    }

    public boolean isTwoVertexConnected(T node, T node2) {
        if (graph.isNeighbor(node, node2)) {
            return true;
        }
        return false;
    }

    public Integer getEdgeCount() {
        if (edgeCount == null) {
            edgeCount = graph.getEdgeCount();
        }
        return edgeCount;
    }

    public Collection<T> getSuccessorsOfVertex(T node) {
        Collection<T> successors = graph.getSuccessors(node);
        return successors;
    }

    public String printAdjecencyMatrix() {
        String userAdjecencies = "";
        Collection<User> vertices = (Collection<User>) graph.getVertices();
        for (User user : vertices) {
            userAdjecencies += user.getUserScreenName() + "  -> ";
            Collection<User> neighbours = (Collection<User>) graph.getNeighbors((T) user);
            for (User neighbourUser : neighbours) {
                userAdjecencies += neighbourUser.getUserScreenName() + ",";
            }
            userAdjecencies += "\n";
        }
        return userAdjecencies;
    }

    public void visualizeGraph() {
        // The Layout<V, E> is parameterized by the vertex and edge types
        Layout<User, String> layout = new KKLayout(graph);

        layout.setSize(new Dimension(1100, 600)); // sets the initial size of the space
        // The BasicVisualizationServer<V,E> is parameterized by the edge types
        BasicVisualizationServer<User, String> vv = new BasicVisualizationServer<User, String>(layout);
        vv.setPreferredSize(new Dimension(1100, 600)); //Sets the viewing area size

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }

    public int getSuccessorEdgeCount(T source, T target) {
        if (graph.isSuccessor(source, target)) {
            return graph.findEdgeSet(source, target).size();
        }
        return 0;
    }

    public void removeOtherVerticesfromGraph(Vector<T> communityVertices) {
        Collection<T> vertices = graph.getVertices();
        List<T> vertices2Remove = new Vector<T>();
        for (T vertice : vertices) {
            if (!communityVertices.contains(vertice)) {
                vertices2Remove.add(vertice);
            }
        }
        for (T vertice : vertices2Remove) {
            graph.removeVertex(vertice);

        }
    }

    public TreeMap<T, Vector<String>> getVertexObjectMapping() {
        return vertexObjectMapping;
    }

    public void setVertexObjectMapping(TreeMap<T, Vector<String>> vertexObjectMapping) {
        this.vertexObjectMapping = vertexObjectMapping;
    }

}

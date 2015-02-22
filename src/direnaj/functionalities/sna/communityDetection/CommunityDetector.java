package direnaj.functionalities.sna.communityDetection;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import direnaj.domain.Community;
import direnaj.domain.User;
import direnaj.functionalities.graph.DirenajGraph;
import direnaj.util.matrix.MatrixElement;

public class CommunityDetector {

    public static DetectedCommunities getCommunitiesInCampaign(double expectedModularityValue,
            DirenajGraph<User> userRelationsGraph, boolean joinAsMuchAsPossible) {
        double modularity = 0d;
        // check expected modularity value
        if (expectedModularityValue <= 0d) {
            expectedModularityValue = 0.3d;
        }
        // form matrix
        HashMap<Integer, Community> communityMapping = makeInitialCommunityMappings4MatrixIndices(userRelationsGraph);
        RealMatrix communityMatrix = calculateMatrix4InitialCommunities(communityMapping, userRelationsGraph);
        //            printMatrix(communityMatrix);
        //printUserRelationsGraph(userRelationsGraph);
        // make n-1 joins
        int vertexCount = userRelationsGraph.getJungGraph().getVertexCount();
        for (int i = 1; i <= vertexCount - 1; i++) {
            MatrixElement matrixElement4Join = getMaxDeltaModularity(communityMatrix);
            modularity += matrixElement4Join.getValue();
            System.out.println("Detected Max Modularity Value : " + matrixElement4Join.toString());
            //printMatrix(communityMatrix, communityMapping, i);
            // if max delta Q_ij is smaller then 0.3, we finish the detection
            if ((!joinAsMuchAsPossible && modularity > 0.3d) || matrixElement4Join.getValue() == 0d) {
                System.out.println("Community Detection is finished in join " + i);
                System.out.println("Modularity Value : " + modularity + " - Found Max Element" + matrixElement4Join);
                break;
            }
            System.out.println("Join : " + i);
            CommunityDetectionIteration newMatrixElements = formNewMatrix(communityMatrix, communityMapping,
                    matrixElement4Join);
            communityMatrix = newMatrixElements.getCommunityMatrix();
            communityMapping = newMatrixElements.getCommunityMapping();
            System.out.println("Community Dimension : " + communityMatrix.getColumnDimension());
            System.out.println("Community Size : " + communityMapping.keySet().size());
        }
        return getDetectedCommunities(communityMapping, modularity, userRelationsGraph.getVertexObjectMapping());
    }

    private static DetectedCommunities getDetectedCommunities(HashMap<Integer, Community> communityMapping,
            double modularity, TreeMap<User, Vector<String>> vertexObjectMapping) {
        // initialize community
        DetectedCommunities communities = new DetectedCommunities(modularity);
        // set communities
        Set<Integer> communityIndices = communityMapping.keySet();
        for (Integer indice : communityIndices) {
            Community community = communityMapping.get(indice);
            community.retrivePostOfUsersInCommunity(vertexObjectMapping);
            communities.getDetectedCommunties(false).add(community);
        }
        communities.setUserCommunityMapping();
        return communities;
    }

    private static CommunityDetectionIteration formNewMatrix(RealMatrix communityMatrix,
            HashMap<Integer, Community> communityMapping, MatrixElement matrixElement4Join) {
        // create new Mapping
        HashMap<Integer, Community> newCommunityMapping = new HashMap<Integer, Community>();
        // create matrix
        int matrixSize = communityMatrix.getColumnDimension();
        RealMatrix newMatrix = new Array2DRowRealMatrix(matrixSize - 1, matrixSize - 1);
        // form new Matrix
        int newMatrixIndice = 0;
        int rowColumn2Remove = matrixElement4Join.getX();
        for (int indice : communityMapping.keySet()) {
            // ignore row of joined community
            if (indice == rowColumn2Remove) {
                continue;
            }
            // delete column of joined community
            RealVector rowVector = communityMatrix.getRowVector(indice);
            RealVector newRowVector = rowVector.getSubVector(0, rowColumn2Remove);
            newRowVector = newRowVector.append(rowVector.getSubVector(rowColumn2Remove + 1, matrixSize
                    - rowColumn2Remove - 1));
            newMatrix.setRowVector(newMatrixIndice, newRowVector);
            // rearrange community mapping
            newCommunityMapping.put(newMatrixIndice, communityMapping.get(indice));
            if (indice == matrixElement4Join.getY()) {
                Community mergedCommunity = newCommunityMapping.get(newMatrixIndice);
                Community community2Merge = communityMapping.get(rowColumn2Remove);
                mergedCommunity.addUsersToCommunity(community2Merge.getUsersInCommunity());
                newCommunityMapping.put(newMatrixIndice, mergedCommunity);
            }
            newMatrixIndice++;
        }
        // make calculation after merge
        // FIXME dikkat edilmesi gereken nokta - biz X'teki community'nin row & column'� siliyoruz
        calculateNewMatrixAfterMerge(communityMatrix, newMatrix, matrixElement4Join, communityMapping);
        calculateNewCommunityFractions(newCommunityMapping);
        System.out.println("New Community Dimension : " + newMatrix.getColumnDimension());
        System.out.println("New Community Size : " + newCommunityMapping.keySet().size());

        return new CommunityDetectionIteration(newCommunityMapping, newMatrix);
    }

    private static void calculateNewCommunityFractions(HashMap<Integer, Community> newCommunityMapping) {
        Set<Integer> communitiesKeySet = newCommunityMapping.keySet();
        for (Integer communityIndice : communitiesKeySet) {
            Community community = newCommunityMapping.get(communityIndice);
            community.calculateNewFraction();
        }
    }

    private static void calculateNewMatrixAfterMerge(RealMatrix communityMatrix, RealMatrix newMatrix,
            final MatrixElement matrixElement4Join, HashMap<Integer, Community> communityMapping) {
        int mergedCommunityIndice = matrixElement4Join.getY();
        if (matrixElement4Join.getY() > matrixElement4Join.getX()) {
            mergedCommunityIndice--;
        }
        // get connected communities to Y
        final Vector<Integer> connectedCommunites2Y = getCommunitiesConnected2Community(communityMatrix,
                matrixElement4Join, matrixElement4Join.getY());
        // get connected communities to Y
        final Vector<Integer> connectedCommunites2X = getCommunitiesConnected2Community(communityMatrix,
                matrixElement4Join, matrixElement4Join.getX());

        for (Integer matriceIndice : connectedCommunites2X) {
            int community2Merge = matriceIndice;
            if (community2Merge > matrixElement4Join.getX()) {
                community2Merge--;
            }
            double mergeValue;

            // Here we check, communities which have connections to both communities.
            // Since we make calculations for communities connected to both community,
            // we do not make calculation in below "for statement" once more.
            if (connectedCommunites2Y.contains(matriceIndice)) {
                mergeValue = communityMatrix.getEntry(matrixElement4Join.getX(), matriceIndice)
                        + communityMatrix.getEntry(matrixElement4Join.getY(), matriceIndice);
            } else {
                mergeValue = communityMatrix.getEntry(matrixElement4Join.getX(), matriceIndice)
                        - (2d * communityMapping.get(matrixElement4Join.getY()).getFractionOfEdges() * communityMapping
                                .get(matriceIndice).getFractionOfEdges());
            }
            newMatrix.setEntry(mergedCommunityIndice, community2Merge, mergeValue);
        }
        for (Integer matriceIndice : connectedCommunites2Y) {
            int community2Merge = matriceIndice;
            if (community2Merge > matrixElement4Join.getX()) {
                community2Merge--;
            }
            double mergeValue;
            if (!connectedCommunites2X.contains(matriceIndice)) {
                mergeValue = communityMatrix.getEntry(matrixElement4Join.getY(), matriceIndice)
                        - (2d * communityMapping.get(matrixElement4Join.getX()).getFractionOfEdges() * communityMapping
                                .get(matriceIndice).getFractionOfEdges());
                newMatrix.setEntry(mergedCommunityIndice, community2Merge, mergeValue);
            }
        }
        // also change the column of merged community
        double[] row = newMatrix.getRow(mergedCommunityIndice);
        newMatrix.setColumn(mergedCommunityIndice, row);
    }

    private static Vector<Integer> getCommunitiesConnected2Community(RealMatrix communityMatrix,
            final MatrixElement matrixElement4Join, int row) {
        int columnDimension = communityMatrix.getColumnDimension();
        final Vector<Integer> connectedCommunites = new Vector<Integer>();
        communityMatrix.walkInRowOrder(new DefaultRealMatrixPreservingVisitor() {
            @Override
            public void visit(int row, int column, double value) {
                if (column != matrixElement4Join.getX() && column != matrixElement4Join.getY() && value != 0) {
                    connectedCommunites.add(column);
                }
            }
        }, row, row, 0, columnDimension - 1);
        return connectedCommunites;
    }

    private static MatrixElement getMaxDeltaModularity(RealMatrix communityMatrix) {
        int matrixDimension = communityMatrix.getColumnDimension();
        final MatrixElement largestElement = new MatrixElement(0, 0, 0d);
        for (int matrixIndice = 1; matrixIndice < matrixDimension; matrixIndice++) {
            communityMatrix.walkInRowOrder(new DefaultRealMatrixPreservingVisitor() {
                @Override
                public void visit(int row, int column, double value) {
                    if (value > largestElement.getValue()) {
                        largestElement.setX(row);
                        largestElement.setY(column);
                        largestElement.setValue(value);
                    }
                }
            }, matrixIndice, matrixIndice, 0, matrixIndice - 1);
        }
        return largestElement;
    }

    private static RealMatrix calculateMatrix4InitialCommunities(HashMap<Integer, Community> communityMapping,
            DirenajGraph<User> userRelationsGraph) {
        double edgeCountInNetwork = Double.valueOf(userRelationsGraph.getEdgeCount());
        System.out.println("All Edges In Network : " + edgeCountInNetwork);
        Set<Integer> communityKeys = communityMapping.keySet();
        int userCount = communityKeys.size();
        RealMatrix communityMatrix = new Array2DRowRealMatrix(userCount, userCount);
        // since first element of first row is diagonal, we do not calculate first row 
        for (int rowIndex : communityKeys) {
            if (rowIndex == 0) {
                continue;
            }
            User rowUser = communityMapping.get(rowIndex).getUsersInCommunity().get(0);
            double degreeOfCommunityX = userRelationsGraph.getVertexDegree(rowUser);
            if (degreeOfCommunityX == 0) {
                continue;
            }
            for (int columnIndex = 0; columnIndex < rowIndex; columnIndex++) {
                User columnUser = communityMapping.get(columnIndex).getUsersInCommunity().get(0);
                double modularityValue;
                double degreeOfCommunityY = userRelationsGraph.getVertexDegree(columnUser);
                System.out.println("Degrees of Users " + rowUser.getUserScreenName() + "-"
                        + columnUser.getUserScreenName() + " for " + rowIndex + " X " + columnIndex + " : "
                        + degreeOfCommunityX + " - " + degreeOfCommunityY);
                if (userRelationsGraph.isTwoVertexConnected(rowUser, columnUser)) {
                    System.out.println("Connected");
                    modularityValue = (1d / (2d * edgeCountInNetwork))
                            - (degreeOfCommunityX * degreeOfCommunityY / Math.pow(2d * edgeCountInNetwork, 2d));
                } else {
                    modularityValue = 0d;
                }
                communityMatrix.setEntry(rowIndex, columnIndex, modularityValue);
                communityMatrix.setEntry(columnIndex, rowIndex, modularityValue);
            }
        }
        return communityMatrix;
    }

    private static HashMap<Integer, Community> makeInitialCommunityMappings4MatrixIndices(
            DirenajGraph<User> userRelationsGraph) {
        Collection<User> allUsersInGraph = userRelationsGraph.getVertices();
        HashMap<Integer, Community> communityInMatrixIndex = new HashMap<Integer, Community>();
        int matrixIndice = 0;
        for (Iterator<User> iterator = allUsersInGraph.iterator(); iterator.hasNext();) {
            User user = (User) iterator.next();
            // set user degree in network
            double userDegree = (double) userRelationsGraph.getJungGraph().degree(user);
            user.setUserDegree(userDegree);
            // calculate ai value (edge frequency of community)
            double communityEdgeFrequency = userDegree / ((double) userRelationsGraph.getEdgeCount() * 2d);
            communityInMatrixIndex.put(matrixIndice, new Community(user, communityEdgeFrequency,
                    (double) userRelationsGraph.getEdgeCount(), userRelationsGraph));
            // increment matrix indice
            matrixIndice++;
        }
        return communityInMatrixIndex;
    }

    private static void printMatrix(RealMatrix communityMatrix, HashMap<Integer, Community> communityMapping,
            int iteration) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("webapps/processResults" + iteration + ".txt", "UTF-8");

            int columnDimension = communityMatrix.getColumnDimension();
            int rowDimension = communityMatrix.getRowDimension();
            writer.println("Printing Matrix");
            writer.println("Row & Column Dimension : " + rowDimension + " X " + columnDimension);
            for (int i = 0; i < columnDimension; i++) {
                double[] row = communityMatrix.getRow(i);
                writer.print("Row : " + i + " -- [");
                for (int j = 0; j < row.length; j++) {
                    writer.print(row[j] + ";");
                }
                writer.println("]");
                //            System.out.println("");
            }
            writer.println("Community Index of Matrix");
            Set<Integer> keySet = communityMapping.keySet();
            for (Integer communityIndex : keySet) {
                Community community = communityMapping.get(communityIndex);
                writer.println("Index : " + communityIndex + " - Community Elements : " + community);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private static void printUserRelationsGraph(DirenajGraph<User> userRelationsGraph) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("webapps/adjecencyMatrix.txt", "UTF-8");
            String adjecencyMatrix = userRelationsGraph.printAdjecencyMatrix();
            writer.println(adjecencyMatrix);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

}

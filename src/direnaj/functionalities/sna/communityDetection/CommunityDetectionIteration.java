package direnaj.functionalities.sna.communityDetection;

import java.util.HashMap;

import org.apache.commons.math3.linear.RealMatrix;

import direnaj.domain.Community;

public class CommunityDetectionIteration {

    // toolkit branch deneme
    private HashMap<Integer, Community> communityMapping;
    private RealMatrix communityMatrix;

    public CommunityDetectionIteration(HashMap<Integer, Community> communityMapping, RealMatrix communityMatrix) {
        this.communityMapping = communityMapping;
        this.communityMatrix = communityMatrix;
    }

    public HashMap<Integer, Community> getCommunityMapping() {
        return communityMapping;
    }

    public RealMatrix getCommunityMatrix() {
        return communityMatrix;
    }

}

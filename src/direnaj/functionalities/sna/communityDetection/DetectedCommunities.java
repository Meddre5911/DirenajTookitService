package direnaj.functionalities.sna.communityDetection;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import direnaj.domain.Community;
import direnaj.domain.User;
import direnaj.util.d3Lib.D3GraphicType;

public class DetectedCommunities {

    private Vector<Community> detectedCommunties;
    private double modularity;
    private TreeMap<User, String> userCommunityMapping; 

    public DetectedCommunities(double modularity) {
        detectedCommunties = new Vector<Community>();
        this.modularity = modularity;
    }

    public Vector<Community> getDetectedCommunties(boolean sort) {
        if (sort) {
            sortCommunities();
        }
        return detectedCommunties;
    }

    private void sortCommunities() {
        Collections.sort(detectedCommunties, new Comparator<Community>() {
            public int compare(Community c2, Community c1) {
                if (c1.getUsersInCommunity().size() == c2.getUsersInCommunity().size())
                    return 0;
                else if (c1.getUsersInCommunity().size() > c2.getUsersInCommunity().size())
                    return 1;
                else
                    return -1;
            }
        });
    }

    public double getModularity() {
        return modularity;
    }

    public String getJsonOfCommunities(D3GraphicType d3GraphicType) throws JSONException {
        String jsonString = null;
        sortCommunities();
        // get json according to graph
        switch (d3GraphicType) {
        case CirclePack:
            JSONObject json = new JSONObject();
            json.put("name", "Communities");
            int i = 1;
            for (Community community : detectedCommunties) {
                JSONObject communityJson = new JSONObject();
                communityJson.put("name", "Community " + i);
                Vector<User> usersInCommunity = community.getUsersInCommunity();
                for (User user : usersInCommunity) {
                    JSONObject userJson = new JSONObject();
                    userJson.put("name", user.getUserScreenName());
                    userJson.put("size", user.getUserDegree());
                    communityJson.accumulate("children", userJson);
                }
                json.accumulate("children", communityJson);
                i++;
            }
            jsonString = json.toString(2);
            break;
        case ForceDirectedGraph:
            TreeMap<User, Integer> userIndexMapping = new TreeMap<User, Integer>();
            JSONObject jsonForcedDirected = new JSONObject();
            int communityIndex = 1;
            int userMappingIndex = 0;
            // get node - community pairs
            for (Community community : detectedCommunties) {
                Vector<User> usersInCommunity = community.getUsersInCommunity();
                for (User user : usersInCommunity) {
                    JSONObject userJson = new JSONObject();
                    userJson.put("name", user.getUserScreenName());
                    userJson.put("group", communityIndex);
                    jsonForcedDirected.accumulate("nodes", userJson);
                    userIndexMapping.put(user, userMappingIndex);
                    userMappingIndex++;
                }
                communityIndex++;
            }
            // get node - node relations
            for (Community community : detectedCommunties) {
                Vector<User> usersInCommunity = community.getUsersInCommunity();
                for (User user : usersInCommunity) {
                    Collection<User> successorsOfUser = community.getSuccessorsOfUser(user);
                    int userIndex = userIndexMapping.get(user);
                    for (User successorUser : successorsOfUser) {
                        int edgeCount = community.getSuccessorEdgeCount(user, successorUser);
                        if (edgeCount > 0) {
                            int successorUserIndex = userIndexMapping.get(successorUser);
                            JSONObject weightJson = new JSONObject();
                            weightJson.put("source", userIndex);
                            weightJson.put("target", successorUserIndex);
                            weightJson.put("value", edgeCount);
                            jsonForcedDirected.accumulate("links", weightJson);
                        }
                    }
                }
            }
            jsonString = jsonForcedDirected.toString(2);
            break;
        case HierarchicalEdgeBundle:
            JSONArray jsonArray = new JSONArray();
            for (Community community : detectedCommunties) {
                // get users in community
                Vector<User> usersInCommunity = community.getUsersInCommunity();
                for (User user : usersInCommunity) {
                    JSONObject userJson = new JSONObject();
                    // put users in to Json with their community name
                    userJson.put("name", community.getCommunityName() + "." + user.getUserScreenName());
                    userJson.put("size", user.getUserDegree());
                    JSONArray userImports = new JSONArray();
                    userJson.put("imports", userImports);
                    // get successor users
                    Collection<User> successorsOfUser = community.getSuccessorsOfUser(user);
                    for (User successorUser : successorsOfUser) {
                        // show relation between sucessor users
                        String successorUserCommunity = userCommunityMapping.get(successorUser);
                        userImports.put(successorUserCommunity + "." + successorUser.getUserScreenName());
                    }
                    jsonArray.put(userJson);
                }
            }
            jsonString = jsonArray.toString(2);
            break;
        }
        return jsonString;
    }

    public TreeMap<User, String> setUserCommunityMapping() {
        userCommunityMapping = new TreeMap<User, String>();
        int communityIndex = 1;
        for (Community community : detectedCommunties) {
            String communityName = "community" + communityIndex;
            community.setCommunityName(communityName);
            Vector<User> usersInCommunity = community.getUsersInCommunity();
            for (User user : usersInCommunity) {
                userCommunityMapping.put(user, communityName);
            }
            communityIndex++;
        }
        return userCommunityMapping;
    }

    public void removeCommunity(Community community) {
        int size = detectedCommunties.size();
        for (int i = 0; i < size; i++) {
            Community targetCommunity = detectedCommunties.get(i);
            if (targetCommunity.getCommunityName().equalsIgnoreCase(community.getCommunityName())) {
                detectedCommunties.remove(i);
                break;
            }
        }

    }

    public void removeCommunities(Vector<Community> communities2Remove) {
        for (Community community : communities2Remove) {
            detectedCommunties.remove(community);
        }
    }
}

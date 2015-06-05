package direnaj.functionalities.semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import net.didion.jwnl.JWNLException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import direnaj.domain.Community;
import direnaj.domain.CommunityInspectorOntologyVocabulary;
import direnaj.domain.User;
import direnaj.functionalities.nlp.ConceptElicitor;

public class CommunityInspectorOntologyHandler {
    private static String ontologySchemaFileUrl = "mergedCommunityInspectorSchema.owl";
    private static int conceptAmountWillBeRetrieved = 5;

    public static OntModel loadModel(OntModel ontModel) {
        ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        ontModel.read(ontologySchemaFileUrl, "RDF/XML");
        return ontModel;
    }

    public static void addCommunityBelongings(OntModel ontModel, Community community) {
        // create Community Individual
        Individual communityIndv = ontModel.createIndividual(
                CommunityInspectorOntologyVocabulary.getIndividualURI(community.getCommunityName(), false),
                CommunityInspectorOntologyVocabulary.COMMUNITY_RSC);
        communityIndv.addLiteral(CommunityInspectorOntologyVocabulary.SIOC_ID_PROP, community.getCommunityName());
        // get Topics and add to Ontology
        addCommunityConcepts(ontModel, community, communityIndv);
        // get all users in community
        Vector<User> usersInCommunity = community.getUsersInCommunity();
        for (User user : usersInCommunity) {
            // create user Individual
            Individual userIndv = ontModel.createIndividual(
                    CommunityInspectorOntologyVocabulary.getIndividualURI(user.getUserScreenName(), false),
                    CommunityInspectorOntologyVocabulary.USER_ACCOUNT_RSC);
            userIndv.addLiteral(CommunityInspectorOntologyVocabulary.SIOC_NAME_PROP, user.getUserScreenName());
            userIndv.addLiteral(CommunityInspectorOntologyVocabulary.SIOC_ID_PROP, user.getUserId());
            // add user interested topics
            addUserConcepts(ontModel, user, userIndv);
            communityIndv.addProperty(CommunityInspectorOntologyVocabulary.BELONGS_TO_PROP, userIndv);
            List<String> posts = user.getPosts();
            for (String tweet : posts) {
                // create post individuals
                Individual postIndv = ontModel.createIndividual(
                        CommunityInspectorOntologyVocabulary.getIndividualURI("POST", true),
                        CommunityInspectorOntologyVocabulary.POST_RSC);
                // add tweet concepts
                addTweetConcepts(ontModel, tweet, postIndv);
                postIndv.addLiteral(CommunityInspectorOntologyVocabulary.CONTENT_PROP, tweet);
                communityIndv.addProperty(CommunityInspectorOntologyVocabulary.BELONGS_TO_PROP, postIndv);
            }
        }

    }

    private static void addTweetConcepts(OntModel ontModel, String tweet, Individual postIndv) {
        try {
            ArrayList<Entry<String, Integer>> tweetRelatedConcepts = ConceptElicitor.tweetRelatedConcepts(tweet, conceptAmountWillBeRetrieved);
            for (Entry<String, Integer> entry : tweetRelatedConcepts) {
                String tweetConcept = entry.getKey();
                Individual tweetTopicIndividual = createTopicIndividual(ontModel, tweetConcept);
                postIndv.addProperty(CommunityInspectorOntologyVocabulary.INTERESTED_IN_PROP, tweetTopicIndividual);
            }
        } catch (JWNLException e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void addUserConcepts(OntModel ontModel, User user, Individual userIndv) {
        try {
            ArrayList<Entry<String, Integer>> userConcepts = ConceptElicitor.getUserConcept(user, conceptAmountWillBeRetrieved);
            for (Entry<String, Integer> entry : userConcepts) {
                String userInterestConcept = entry.getKey();
                Individual userInterestedTopic = createTopicIndividual(ontModel, userInterestConcept);
                userIndv.addProperty(CommunityInspectorOntologyVocabulary.INTERESTED_IN_PROP, userInterestedTopic);
            }
        } catch (JWNLException e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void addCommunityConcepts(OntModel ontModel, Community community, Individual communityIndv) {
        try {
            ArrayList<Entry<String, Integer>> communityConcept = ConceptElicitor.getCommunityConcept(
                    community.getUsersInCommunity(), conceptAmountWillBeRetrieved);
            for (Entry<String, Integer> entry : communityConcept) {
                String interestedTopic = entry.getKey();
                Individual topicIndv = createTopicIndividual(ontModel, interestedTopic);
                communityIndv.addProperty(CommunityInspectorOntologyVocabulary.INTERESTED_IN_PROP, topicIndv);
            }
        } catch (JWNLException e) {
            // XXX Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Individual createTopicIndividual(OntModel ontModel, String interestedTopic) {
        Individual topicIndv = ontModel.createIndividual(
                CommunityInspectorOntologyVocabulary.getIndividualURI(interestedTopic, true),
                CommunityInspectorOntologyVocabulary.TOPIC_RSC);
        topicIndv.addLiteral(CommunityInspectorOntologyVocabulary.SIOC_NAME_PROP, interestedTopic);
        return topicIndv;
    }

}

package direnaj.domain;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Vector;

import net.didion.jwnl.JWNLException;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import direnaj.functionalities.nlp.ConceptElicitor;
import direnaj.functionalities.semantic.CommunityInspectorOntologyHandler;
import direnaj.functionalities.sna.communityDetection.DetectedCommunities;

public class CommunityInspectorOntology {

    private OntModel ontModel;

    public CommunityInspectorOntology() {
        ontModel = CommunityInspectorOntologyHandler.loadModel(ontModel);
    }

    public OntModel getOntModel() {
        return ontModel;
    }

    public void addDetectedCommunities(DetectedCommunities communities) {
        Vector<Community> detectedCommunties = communities.getDetectedCommunties(false);
        for (Community community : detectedCommunties) {
            CommunityInspectorOntologyHandler.addCommunityBelongings(ontModel, community);
        }
        calculateTopicSimilarities();
        ontModel.write(System.out);
    }

    public void calculateTopicSimilarities() {
        ExtendedIterator<Individual> listIndividuals = ontModel
                .listIndividuals(CommunityInspectorOntologyVocabulary.TOPIC_RSC);
        ExtendedIterator<Individual> listIndividuals2 = ontModel
                .listIndividuals(CommunityInspectorOntologyVocabulary.TOPIC_RSC);
        for (Individual topicIndv : listIndividuals.toList()) {
            String topic = (String) topicIndv.getProperty(CommunityInspectorOntologyVocabulary.SIOC_NAME_PROP)
                    .getLiteral().getValue();
            ArrayList<Entry<String, Integer>> concepts1 = createNewArrayList(topic);

            for (Individual comparedTopicIndv : listIndividuals2.toList()) {
                if (!topicIndv.equals(comparedTopicIndv)) {
                    String topic2 = (String) comparedTopicIndv
                            .getProperty(CommunityInspectorOntologyVocabulary.SIOC_NAME_PROP).getLiteral().getValue();
                    ArrayList<Entry<String, Integer>> concepts2 = createNewArrayList(topic2);
                    try {
                        double similarity = ConceptElicitor.conceptSetSimilarity(concepts1, concepts2,true);
                        if (similarity > 0) {
                            Individual conceptSimilarity = ontModel.createIndividual(
                                    CommunityInspectorOntologyVocabulary.getIndividualURI("ConceptSimilarity", true),
                                    CommunityInspectorOntologyVocabulary.CONCEPT_SIMILARITY_RSC);
                            conceptSimilarity.addProperty(CommunityInspectorOntologyVocabulary.IS_CALCULATED_FOR_PROP,topicIndv);
                            conceptSimilarity.addProperty(CommunityInspectorOntologyVocabulary.IS_CALCULATED_FOR_PROP,comparedTopicIndv);
                            conceptSimilarity.addLiteral(CommunityInspectorOntologyVocabulary.HAS_SIMILARITY_VALUE_PROP, Double.valueOf(similarity));
                        }

                    } catch (JWNLException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private ArrayList<Entry<String, Integer>> createNewArrayList(final String topic) {
        ArrayList<Entry<String, Integer>> arrayList = new ArrayList<>();
        arrayList.add(new Entry<String, Integer>() {

            @Override
            public Integer setValue(Integer value) {
                // TODO Auto-generated method stub
                return 1;
            }

            @Override
            public Integer getValue() {
                // TODO Auto-generated method stub
                return 1;
            }

            @Override
            public String getKey() {
                // TODO Auto-generated method stub
                return topic;
            }
        });

        // TODO Auto-generated method stub
        return arrayList;
    }

}

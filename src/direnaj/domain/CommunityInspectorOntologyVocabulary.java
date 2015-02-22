package direnaj.domain;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class CommunityInspectorOntologyVocabulary {

    public static final String SIOC_BASE_URI = "http://rdfs.org/sioc/ns#";

    public static final String POC_BASE_URI = "http://www.cmpe.boun.edu.tr/soslab/ontologies/poc#";

    public static final String COMMUNITY_INSPECTOR_BASE_URI = "http://www.cmpe.boun.edu.tr/soslab/ontologies/communityInspector#";

    public static final String COMMUNITY_INSPECTOR_INDV_BASE_URI = "http://communityInspector.com#";

    public static final String COMMUNITY = SIOC_BASE_URI + "Community";

    public static final String USER_ACCOUNT = SIOC_BASE_URI + "UserAccount";

    public static final String CONTENT = SIOC_BASE_URI + "content";

    public static final String POST = SIOC_BASE_URI + "Post";

    public static final String SIOC_ID = SIOC_BASE_URI + "id";

    public static final String SIOC_NAME = SIOC_BASE_URI + "name";

    public static final String BELONGS_TO = POC_BASE_URI + "belongs_to";

    public static final String INTERESTED_IN = COMMUNITY_INSPECTOR_BASE_URI + "interestedIn";

    public static final String TOPIC = COMMUNITY_INSPECTOR_BASE_URI + "Topic";

    public static final String IS_CALCULATED_FOR = COMMUNITY_INSPECTOR_BASE_URI + "isCalculatedFor";

    public static final String HAS_SIMILARITY_VALUE = COMMUNITY_INSPECTOR_BASE_URI + "hasSimilarityValue";

    public static final String CONCEPT_SIMILARITY = COMMUNITY_INSPECTOR_BASE_URI + "ConceptSimilarity";
    
    

    // Resources
    public static final Resource COMMUNITY_RSC = resource(COMMUNITY);

    public static final Resource USER_ACCOUNT_RSC = resource(USER_ACCOUNT);

    public static final Resource POST_RSC = resource(POST);

    public static final Resource TOPIC_RSC = resource(TOPIC);
    
    public static final Resource CONCEPT_SIMILARITY_RSC = resource(CONCEPT_SIMILARITY);

    // Properties
    public static final Property BELONGS_TO_PROP = property(BELONGS_TO);

    public static final Property CONTENT_PROP = property(CONTENT);

    public static final Property SIOC_ID_PROP = property(SIOC_ID);

    public static final Property SIOC_NAME_PROP = property(SIOC_NAME);

    public static final Property INTERESTED_IN_PROP = property(INTERESTED_IN);

    public static final Property IS_CALCULATED_FOR_PROP = property(IS_CALCULATED_FOR);
    
    public static final Property HAS_SIMILARITY_VALUE_PROP = property(HAS_SIMILARITY_VALUE);
    
    
    
    

    public static String getIndividualURI(String individualName, boolean addRandomNumber) {
        String uri = COMMUNITY_INSPECTOR_INDV_BASE_URI + individualName;
        if (addRandomNumber) {
            uri += Math.random();
        }
        return uri;
    }

    /**
     * creates {@link Property} according to parameter
     * 
     * @param uri
     *            given parameter for which property should be created
     * @return {@link Property} instance
     */
    private static Property property(String uri) {

        return ResourceFactory.createProperty(uri);
    }

    /**
     * creates resources for given parameters
     * 
     * @param uri
     *            uri of the wanted resource
     * @return {@link Resource} instance
     */
    private static Resource resource(String uri) {
        Resource resource = ResourceFactory.createResource(uri);
        return resource;
    }

}

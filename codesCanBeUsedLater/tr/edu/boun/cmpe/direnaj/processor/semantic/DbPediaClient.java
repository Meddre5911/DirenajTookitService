package semantic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;
import org.apache.commons.lang.StringUtils;
import org.dbpedia.spotlight.annotate.DefaultAnnotator;
import org.dbpedia.spotlight.exceptions.ConfigurationException;
import org.dbpedia.spotlight.exceptions.InputException;
import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;

import util.Util;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntTools;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL;

public final class DbPediaClient {
	
	private static final String RESULT = "result";

	private static final String SERVICE_URI_DBPEDIA = "http://dbpedia.org/sparql";
	
	private static final String PREFIX = 	
				"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
				"PREFIX : <http://dbpedia.org/resource/> " +
				"PREFIX dbpedia2: <http://dbpedia.org/property/> " +
				"PREFIX dbpedia: <http://dbpedia.org/> " +
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
				"PREFIX dcterms: <http://purl.org/dc/terms/>";

	private static final String LANG_EN = "@en";
	
    private static void testPath(OntTools.Path path, Property[] expected) {

        int i = 0;
        Iterator j = path.iterator();
        while (j.hasNext()) {
        	System.out.println("path position: " + i);
        	boolean ok = expected[i] == ((Statement) j.next()).getPredicate();
            i++;
        }
    }
    

	
	public static List<String> categories(String resource) {
		
		String queryString = " SELECT (?category_label as ?" + RESULT + ")" + 
				 "  WHERE { " +
			     resource + " dcterms:subject ?category . " +
				"?category rdfs:label ?category_label . " +  
			     "FILTER langMatches( lang(?category_label), 'en')" +
			     "}";
		
		return queryDbPedia(queryString);		
	}
	
	/**
	 * 
	 * @param resource
	 * @return
	 */
	public static String findAbstract(String resource) {

		String queryString = " SELECT (?abstract as ?" + RESULT + ")" + 
				 "  WHERE { " +
			     resource + " <http://dbpedia.org/ontology/abstract> ?abstract ." +
			     "FILTER langMatches( lang(?abstract), 'en')" +
			     "}";

		return queryDbPedia(queryString).get(0);
	}
	
	/**
	 * 
	 * @param keyword
	 * @return
	 */
	public static List<String> categoriesOfTopicsIncludingKeyword(String keyword) {
	
		String queryString = " SELECT distinct ?" + RESULT + 
							 "  WHERE { " +
						     "?androids rdfs:label ?androidsLabelDesc . " +
						     "?androidsLabelDesc <bif:contains> \"" + keyword + "\"@en . " + 
						     "?androids dcterms:subject ?category ." +
						     "?category rdfs:label ?category_label ." +
						     "FILTER langMatches( lang(?category_label), 'en')" +
						     "}";

		return queryDbPedia(queryString);
	}
	
	/**
	 * @deprecated
	 * 
	 * @param keyword
	 * @return
	 */
	private static List<String> broaderSearch(String keyword) {
		
		String queryString = 
			
				"SELECT * WHERE {" +
				"	?s rdfs:label \"" + keyword + "\"@en." +
				"	?s skos:subject ?o." +
				"	?o skos:broader ?oo" +
				"	}";
				/*
				"SELECT DISTINCT ?m ?n ?p ?d"+
				"WHERE {"+
				" ?m rdfs:label ?n."+
				" ?m skos:subject ?c."+
				" ?c skos:broader category:Churches_in_Paris."+
				" ?m p:abstract ?d."+
				" ?m geo:point ?p"+
				" FILTER ( lang(?n) = \"fr\" )"+
				" FILTER ( lang(?d) = \"fr\" )"+
				" }";
				 */
				
		return queryDbPedia(queryString);
	}


	private static List<String> queryDbPedia(String queryString) {
		return query(SERVICE_URI_DBPEDIA, queryString);
	}
	
	private static List<String> query(String serviceUri, String queryString) {
		
		List<String> list = new ArrayList<String>();
		
		ResultSet results = null;
		
		// now creating query object
		Query query = QueryFactory.create(PREFIX + queryString);
		
		// System.out.println(query.toString());
		
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceUri, query);

		//after it goes standard query execution and result processing which can
		// be found in almost any Jena/SPARQL tutorial.
		try {
		    results = qexec.execSelect();
		    
		    // ResultSetFormatter.out(System.out, results, query);
		    
		    while (results.hasNext()) {
		    	list.add(
		    			StringUtils.substringBefore(results.next().get(RESULT).toString(), LANG_EN)
		    			
		    			);
		    }
		}
		finally {
		   qexec.close();
		}
		
		return list;
	}

	/**
	 * 
	 */
	private DbPediaClient(){
		throw new AssertionError();
	}
	
	public static void main4(String[] args) {
		
		//String[] terms = {"oakland","october","ows","park","police","politics","protest","protesters","protests","square","zuccotti"};
		String[] terms = {"archives", "authorities", "books", "cataloging", "coding", "community", "conferences", "data", "database", "datacite", "description", "digital_collections", "dish2012", "eac", "ead", "ebook", "find", "framework", "future", "geospatial", "geospatial", "linkeddata", "rdf", "geosparql", "scovo", "maps", "visualization", "semweb", "google", "googlerefine", "gui", "hack", "hacking", "howto", "ifttt", "ipad", "ipadapps", "javascript", "kb11", "labs", "lams", "libraries", "library", "libraryhack", "librsrock", "linkeddata", "linked_data", "lodlam", "mads", "mashups", "mesh", "metadata", "mlascarides", "ndsa", "new_york", "nypl", "ontologies", "ontology", "open", "openculture", "opendata", "openvisualization", "owl", "peerreview", "rdf", "reference", "refine", "research", "semantic", "semanticweb", "semweb", "skos", "socialnetworking", "standards", "technology", "tools", "tutorial", "tutorials", "twitter_friends", "usability", "ux-"};
		
		Bag<String> categoryBag = new TreeBag<String>();

		for (String term : terms) {
			
			System.out.println("term:" + term);
			
			for (String category : categoriesOfTopicsIncludingKeyword(term)) {
				categoryBag.add(category);
			}
			
			System.out.println("--------------------------------------------------");
		}
		
		Bag<String> topCategories = Util.findTopElems(categoryBag, 5);
		for (String category : topCategories.uniqueSet() ) {
			System.out.println(category.replace(',', '-')+ "," + topCategories.getCount(category));
		}
		
	}
	
	public static void main3(String[] args) throws ConfigurationException, InputException {
		/*
		List<String> categories = DbPediaClient.categoriesOfTopics("java_island");
		
		for (String category : categories) {
			System.out.println(category);
		}
		*/
		DefaultAnnotator annotator = new DefaultAnnotator(null, null);

		List<DBpediaResourceOccurrence> resourceOccurrences = annotator.annotate("Justin Drew Bieber is a Canadian pop/R&B singer, songwriter and actor.");
		
		for (DBpediaResourceOccurrence resourceOccurrence : resourceOccurrences) {
			System.out.println(resourceOccurrence);
		}
		
	}
	
	public static void main2(String[] args) {
		
		String resource = "<http://dbpedia.org/resource/Java_%28programming_language%29>";
		
		List<String> categoriesOfTopic = categories(resource);
		
		for (String string : categoriesOfTopic) {
			System.out.println(string);
		}
		
		System.out.println(findAbstract(resource));
		
	}
	
	public static void main1(String[] args) {

		// OntTools.findShortestPath(arg0, arg1, arg2, arg3)
		
		String NS = "http://test.linkedmdb.org";
		OntModel m_model;

        OntClass m_a;
        OntClass m_b;
        OntClass m_c;
        OntClass m_d;
        OntClass m_e;
        OntClass m_f;
        OntClass m_g;
        OntClass m_top;
        
		 m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
         m_a = m_model.createClass(NS + "A");
         m_b = m_model.createClass(NS + "B");
         m_c = m_model.createClass(NS + "C");
         m_d = m_model.createClass(NS + "D");
         m_e = m_model.createClass(NS + "E");
         m_f = m_model.createClass(NS + "F");
         m_g = m_model.createClass(NS + "G");
         m_top = m_model.createClass(OWL.Thing.getURI());
         
         
         Property p = m_model.createProperty(NS + "p");
         m_a.addProperty(p, m_b);

         testPath(OntTools.findShortestPath(m_model, m_a, m_b,Filter.any), new Property[] { p });
	}

}

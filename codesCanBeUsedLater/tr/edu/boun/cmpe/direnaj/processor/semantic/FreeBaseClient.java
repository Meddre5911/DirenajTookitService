package semantic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import util.JsonUtil;
import util.Util;

public class FreeBaseClient {
	
	private static final String ELEM_ALL = "*";

	private static final String ELEM_GUID = "guid";
	
	private static final String URL_RAW_CONTENT = "http://api.freebase.com/api/trans/raw/guid/";
	private static final String[] RAW_DESC_TAGS = {"<p>", "</p>"};
	
	public static final String RDF_URL_TEMPLATE = "<http://rdf.freebase.com/ns{x}>";
	
	private static final String ELEM_CODE = "code";
	private static final String ELEM_STATUS = "status";
	private static final String ELEM_MID = "mid";
	private static final String ELEM_RESULT = "result";
	private static final String ELEM_MESSAGES = "messages";
	private static final String ELEM_MESSAGE = "message";
	
	private static final String X_QUERY = "x_query",
	                           MQL_READ = "https://api.freebase.com/api/service/mqlread?query={\"query\":" + X_QUERY + "}";
	
	private static final String X_ID = "x_id",
	                            Y_ID = "y_id",
	                            QUERY_WITH_ID = "[{\"id\":\"" + X_ID + "\",\"" + Y_ID + "\":[]}]";
	
	private static final String STATUS_OK = "200 OK";

	private static final String CODE_OK = "/api/status/ok"; 
	private static final String CODE_ERROR = "/api/status/error"; 

	
	
	public static final FreeBaseClient INSTANCE = new FreeBaseClient();
	private FreeBaseClient() {}

	/**
	 * 
	 * @param guid
	 * @return
	 * @throws IOException
	 */
	private String fetchTopicDesc(String guid) throws IOException {
		return StringUtils.substringBetween(Util.fetchSource(URL_RAW_CONTENT.concat(guid)), RAW_DESC_TAGS[0], RAW_DESC_TAGS[1]);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public FreeBaseTopic fetchTopic(String id) throws MalformedURLException, IOException {
		return enhanceTopic(id, null);
	}
	
	public FreeBaseTopic enhanceTopic(FreeBaseTopic topic) throws MalformedURLException, IOException {
		return enhanceTopic(topic.getId(), topic);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private FreeBaseTopic enhanceTopic(String id, FreeBaseTopic topic) throws MalformedURLException, IOException {
		
		if (topic==null) {
			topic = new FreeBaseTopic(id, null);
		}
		
		JSONObject root = (JSONObject)JSONValue.parse(buildReader(id, ELEM_ALL));
		
		String status = (String)root.get(ELEM_STATUS);
		String code = (String)root.get(ELEM_CODE);
		
		if (STATUS_OK.equals(status)) {
			
			if (CODE_OK.equals(code)) {
				
				JSONObject result = (JSONObject)(JsonUtil.getJsonArray(ELEM_RESULT, root).get(0));
				
				JSONArray rdfIds = JsonUtil.getJsonArray(ELEM_MID, result);
				topic.setMid(JsonUtil.toStringArr(rdfIds));
				
				topic.setGuid( JsonUtil.getJsonArray(ELEM_GUID, result).get(0).toString().substring(1) );

				// topic.setDescription(fetchTopicDesc(topic.getGuid())); //FIXME

			} else if (CODE_ERROR.equals(code)) {
				
				JSONArray messages = JsonUtil.getJsonArray(ELEM_MESSAGES, root);

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < messages.size(); i++) {
					sb.append( ((JSONObject)messages.get(i)).get(ELEM_MESSAGE) );
					sb.append("; ");
				}
				
				throw new RuntimeException(sb.toString());	

			} else {
				throw new RuntimeException("Unknown code: " + code);	
			}
			
		} else {
			throw new RuntimeException(status);
		}
		
		return topic;
	}
	
	/**
	 * @deprecated use {@link #fetchTopic(String)}
	 * 
	 * @param id
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String[] fetchRdfIds(String id) throws MalformedURLException, IOException {

		JSONObject root = (JSONObject)JSONValue.parse(buildReader(id, ELEM_MID));
		
		String status = (String)root.get(ELEM_STATUS);
		String code = (String)root.get(ELEM_CODE);
		
		if (STATUS_OK.equals(status)) {
			
			if (CODE_OK.equals(code)) {
				
				JSONArray result = JsonUtil.getJsonArray(ELEM_RESULT, root);
				
				JSONArray rdfIds = JsonUtil.getJsonArray(ELEM_MID, (JSONObject)result.get(0));
				
				System.out.println(rdfIds);
				
				return JsonUtil.toStringArr(rdfIds);
				
			} else if (CODE_ERROR.equals(code)) {
				
				JSONArray messages = JsonUtil.getJsonArray(ELEM_MESSAGES, root);

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < messages.size(); i++) {
					sb.append( ((JSONObject)messages.get(i)).get(ELEM_MESSAGE) );
					sb.append("; ");
				}
				
				throw new RuntimeException(sb.toString());	

			} else {
				throw new RuntimeException("Unknown code: " + code);	
			}
			
		} else {
			throw new RuntimeException(status);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param result
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private Reader buildReader (String id, String result) throws MalformedURLException, IOException {
		
		String url = MQL_READ.replace(X_QUERY, QUERY_WITH_ID.replace(X_ID, id).replace(Y_ID, result));
		System.out.println(url);
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	}

	public static void main(String[] args) throws Exception {
		
		FreeBaseTopic topic = INSTANCE.fetchTopic("/m/07sbkfb");
		
		System.out.println(topic);
		
		Collection<String> entites = WikiMetaExtractor.INSTANCE.extractNamedEntites(topic.getDescription());
		
		for (String string : entites) {
			System.out.println(string);
		}
		
	}
	
}

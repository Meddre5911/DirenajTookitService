package semantic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import util.JsonUtil;

public final class WikiMetaExtractor {
	
	public static final WikiMetaExtractor INSTANCE = new WikiMetaExtractor();
    
    public enum Format {
    	XML("xml"),
		JSON("json");
    	
        private final String value;
        
        Format(String value) {this.value = value;}
        
        public String getValue() {return this.value;}
    }
    
    private static String API_URL = "http://www.wikimeta.org/perl/semtag.pl";
    private static String API_KEY = "bcelebi";
    
    private static String LANG_EN = "EN";
   
	private WikiMetaExtractor() {}
    
    public BufferedReader getBufferedReader(String content) throws Exception {
    	return getBufferedReader(API_KEY, Format.JSON, content, LANG_EN);	
    }
    
    public Collection<String> extractNamedEntites(String content) throws Exception {
    	
    	Collection<String> entites = new ArrayList<String>();
    	
    	BufferedReader bufferedReader = getBufferedReader(content);
    	
    	JSONObject root = (JSONObject)JSONValue.parse(bufferedReader);

    	JSONArray namedEntities = JsonUtil.getJsonArray("Named Entities", root);
    	
    	for (int i = 0; i < namedEntities.size(); i++) {
			entites.add( ((JSONObject)namedEntities.get(i)).get("EN").toString() );
		}
    	
    	return entites;
    }
    
    /*
     * Preformated method needs only api key, return format, string, language
     * 
     */
	public BufferedReader getBufferedReader(String apiKey, Format format, String content, String lng) throws Exception {
		return getBufferedReader(apiKey, format, content, 10, 100, lng ,true);
	}

    /*
     * Full method
     * 
     */
    public BufferedReader getBufferedReader(String apiKey, Format format, String content, int treshold, int span, String lng, boolean semtag) throws Exception {    

    	String callFormat = format.value;
        
        URL url = new URL(API_URL);
        HttpURLConnection server = (HttpURLConnection)url.openConnection();
        server.setDoInput(true);
        server.setDoOutput(true);
        server.setRequestMethod("POST");
        server.setRequestProperty("Accept", callFormat );
        server.connect();
        
        BufferedWriter bw = new BufferedWriter(
                            new OutputStreamWriter(
                                server.getOutputStream()));
        String semtagS = "0";
        if(semtag)semtagS = "1";
        
        String request = "treshold="+treshold+"&span="+span+"&lng="+lng+"&semtag="+semtagS+"&api="+apiKey+"&contenu="+content;
        bw.write(request, 0, request.length());
        bw.flush();
        bw.close();
        
        return new BufferedReader(new InputStreamReader(server.getInputStream()));
        
        /*
        String ligne;
        while ((ligne = reader.readLine()) != null) {
            result += ligne+"\n";
        }
        
        reader.close();
        server.disconnect();
        */
    }
    
   
}

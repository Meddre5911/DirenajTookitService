package direnaj.adapter;

import java.io.Reader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

public class DirenajDataHandler {
	
	public static JSONObject getData(String userID, String password, String campaignId, int skip, int limit) 
			throws Exception, DirenajInvalidJSONException  {
		
		// URL to the direnaj system
		String urlStr = "http://direnaj-staging.cmpe.boun.edu.tr/statuses/filter";
		
		URL urlObj = new URL(urlStr);
		
		// establishing connection
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
		
		
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		
		String urlParams = 
				"auth_user_id=" + userID +
				"&auth_password=" + password + 
				"&campaign_id=" + campaignId +
				"&skip=" + skip + 
				"&limit=" + limit;
		
		// connecting to the http output stream on which we are going to write
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		
		// writing the request
		wr.writeBytes(urlParams);
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + urlStr);
		System.out.println("Post parameters : " + urlParams);
		System.out.println("Response Code : " + responseCode);
 
		// we only handle http 200 now, 
		// XXX 401 handling would also be fine
		// XXX handle other response codes
		if (responseCode==200) {
			
			
			Reader reader = new InputStreamReader(con.getInputStream());
			char[] buf = new char[2048];
			
			// not doing this with BufferedReader, 
			// because we have now the control to load as many 
			// bytes as necessary, instead of loading the whole thing
			StringBuffer responseBuffer = new StringBuffer();
			// we are, in fact, loading everything right now, but we 
			// have the power nevertheless :)
	 
			int red = -1;
			while ((red = reader.read(buf)) != -1) {
				responseBuffer.append(buf, 0, red);
			}
			reader.close();
			
			JSONObject responseObj = null;
			try {
				
				Locale.setDefault(new Locale("tr", "TR"));
				
				responseObj = new JSONObject(responseBuffer.toString());
				
			} catch (JSONException je) {
				throw new DirenajInvalidJSONException(je);
			}
			
			
			return responseObj;
		}
		else {
			// if the response code is not 200, we throw our
			// exception to report that there is something wrong 
			// with the response
			throw new DirenajResponseException(responseCode + " - " + con.getResponseMessage());	
		}
		
	}

}

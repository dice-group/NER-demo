package org.dice_research.namedEntityRecognition;

import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.logging.log4j.LogManager;

/**
 * NER using DBpedia Spotlight API 
 *
 */
public class Spotlight 
{    
    private String requestURL = "http://model.dbpedia-spotlight.org/en/annotate";
	private String confidence = "0.5"; // score for disambiguation / linking
	private String support = "20";	// number of inlinks in Wikipedia
	
	/**
	 * @param inputText
	 * @return URIs of NEs after post-processing
	 * */
	public List<String> getEntities(final String inputText) throws MalformedURLException, IOException, ProtocolException, ParseException {

		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&confidence=" + confidence;
		urlParameters += "&support=" + support;

		return postProcessing(requestPOST(urlParameters, requestURL));
	}
	
	/**
	 * 
	 * @param urlParameters
	 * @param requestURL
	 * @return response as String from the requestURL
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	
	private String requestPOST(final String urlParameters, final String requestURL) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(requestURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.length()));

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();

		InputStream inputStream = connection.getInputStream();
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(in);

		StringBuilder sb = new StringBuilder();
		while (reader.ready()) {
			sb.append(reader.readLine());
		}

		wr.close();
		reader.close();
		connection.disconnect();

		return sb.toString();
	}
	
	/**
	 * 
	 * @param response from the requestURL
	 * @return List of namedEnties
	 * @throws ParseException
	 */
	
	public List<String> postProcessing(final String response) throws ParseException{
		
		List<String> namedEntities = new ArrayList<>();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(response);

		JSONArray resources = (JSONArray) jsonObject.get("Resources");
		if (resources != null) {
			for (Object res : resources.toArray()) {
				JSONObject next = (JSONObject) res;
				String uri = ((String) next.get("@URI"));
				namedEntities.add(uri);
			}
		}
		return namedEntities;
	}
	

    public static void main( String[] args ) throws MalformedURLException, ProtocolException, IOException, ParseException
    {	
    Spotlight ap = new Spotlight();
    String str = "Katie Holmes got divorced from Tom Cruise in Germany.";
    System.out.println(ap.getEntities(str));
        
    }
}

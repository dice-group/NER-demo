package org.dice_research.namedEntityRecognition;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Fox {
		private String requestURL = "http://fox-demo.aksw.org/fox";
		private String outputFormat = "N-Triples";
		private String taskType = "ner";
		private String inputType = "text";
		
		/**
		 * 
		 * @param inputText
		 * @param lang
		 * @return response from the Fox endpoint
		 * @throws Exception
		 */

		private String doTASK(String inputText, String lang) throws Exception {
			JSONObject urlParameters = new JSONObject();
			
			urlParameters.put("type", inputType);
			urlParameters.put("task", taskType);
			urlParameters.put("lang", lang);

			urlParameters.put("output", outputFormat);
			urlParameters.put("input", URLEncoder.encode(inputText, "UTF-8"));
			return requestPOST(urlParameters, requestURL);
			
		}

		/**
		 *  
		 * @param question
		 * @param lang
		 * @return list of namedEntities
		 * @throws Exception
		 */
		
		public List<String> getEntities(String question, String lang) throws Exception {

			String foxJSONOutput = doTASK(question, lang);

			JSONParser parser = new JSONParser();
			JSONObject jsonArray = (JSONObject) parser.parse(foxJSONOutput);
			String output = URLDecoder.decode((String) jsonArray.get("output"), "UTF-8");

			String baseURI = "http://dbpedia.org";
			Model model = ModelFactory.createDefaultModel();
			RDFReader r = model.getReader("N3");
			r.read(model, new StringReader(output), baseURI);

			ResIterator iter = model.listSubjects();
			List<String> namedEntities = new ArrayList<>();
			while (iter.hasNext()) {
				Resource next = iter.next();
				StmtIterator statementIter = next.listProperties();

				while (statementIter.hasNext()) {
					Statement statement = statementIter.next();
					String predicateURI = statement.getPredicate()
					                               .getURI();
					namedEntities.add(predicateURI);
				}
			}

			return namedEntities;
		}
		
		/**
		 * 
		 * @param urlParameters
		 * @param requestURL
		 * @return 
		 * @throws MalformedURLException
		 * @throws IOException
		 * @throws ProtocolException
		 */
		
		private String requestPOST(final JSONObject urlParameters, final String requestURL) throws MalformedURLException, IOException, ProtocolException {
			URL url = new URL(requestURL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters.toString());
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
		
		public static void main( String[] args ) throws Exception{	
	    Fox f = new Fox();
	    String str = "Katie Holmes got divorced from Tom Cruise in Germany.";
	    System.out.println(f.getEntities(str, "en"));
	    }
}

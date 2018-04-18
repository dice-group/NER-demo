package org.dice_research.namedEntityRecognition;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONObject;

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
		urlParameters.put("input", inputText);
		return requestPOST(urlParameters, requestURL);

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

	public static void main(String[] args) throws Exception {
		Fox f = new Fox();
		String str = "Katie Holmes got divorced from Tom Cruise in Germany.";
		System.out.println(f.doTASK(str, "en"));
	}
}

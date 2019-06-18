package com.comparator.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class Util {

	private static String content1 = null;
	private static String content2 = null;
	public static String contenttypevalue;
	static ObjectMapper mapper = new ObjectMapper();
	public static Util http = new Util();
	static List<String> outputs = new ArrayList<String>();

	public static List<String> urlReader(String s1, String s2) throws IOException {
		outputs = new ArrayList<String>(0);
		char c = File.separatorChar;
		String f1 = System.getProperty("user.dir") + c + "src" + c + "main" + c + "resources" + c + "Files" + c + s1
				+ ".txt";
		String f2 = System.getProperty("user.dir") + c + "src" + c + "main" + c + "resources" + c + "Files" + c + s2
				+ ".txt";
		BufferedReader reader1 = null, reader2 = null;
		try {
			reader1 = new BufferedReader(new FileReader(f1));
			reader2 = new BufferedReader(new FileReader(f2));
			for (String line1 = reader1.readLine(), line2 = reader2.readLine(); line1 != null
					&& line2 != null; line1 = reader1.readLine(), line2 = reader2.readLine()) {
				if (verifyValidUrl(line1) && verifyValidUrl(line2)) {
					outputs.add(Util.consumeData(line1, line2));
				} else {
					outputs.add("URLs are not valid");
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			reader1.close();
			reader2.close();
		}
		return outputs;
	}

	public static boolean verifyValidUrl(String urllink) {
		try {
			URL url = new URL(urllink);
			url.toURI();
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	public static String consumeData(String url1, String url2) throws Exception {
		content1 = getDataByUrl(url1);

		content2 = getDataByUrl(url2);

		boolean flag = false;
		if ("application/json; charset=utf-8".equalsIgnoreCase(contenttypevalue)) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode1 = mapper.readTree(content1);
			JsonNode jsonNode2 = mapper.readTree(content2);
			flag = compareByJsonNode(jsonNode1, jsonNode2, url1, url2);
		} else if ("application/xml; charset=UTF-8".equalsIgnoreCase(contenttypevalue)) {

			JsonNode jsonNode1 = getJsonNode(content1);
			JsonNode jsonNode2 = getJsonNode(content2);
			flag = compareJsons(jsonNode1, jsonNode2);
		} else {
			return "Input api's are of not of xml/json type";
		}

		if (flag) {
			return url1 + "    EQUALS  " + url2;
		} else {
			return url1 + "  NOT EQUALS  " + url2;
		}
	}

	public static String getDataByUrl(String url) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		contenttypevalue = con.getContentType().trim();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		String data = (response.toString());
		return data;
	}

	private static JsonNode getJsonNode(String xml) throws Exception {
		XmlMapper xmlMapper = new XmlMapper();
		JsonNode jsonNode = xmlMapper.readTree(xml.getBytes());
		String json = mapper.writeValueAsString(jsonNode);
		return xmlMapper.readTree(xml.getBytes());
	}

	private static boolean compareJsons(JsonNode jsonNode1, JsonNode jsonNode2) throws Exception {
		String json1 = mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(mapper.readValue(jsonNode1.toString(), Object.class));
		String json2 = mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(mapper.readValue(jsonNode2.toString(), Object.class));
		JSONCompareResult result = JSONCompare.compareJSON(json1, json2, JSONCompareMode.STRICT);
		return result.passed();
	}

	private static boolean compareByJsonNode(JsonNode jsonNode1, JsonNode jsonNode2, String url1, String url2)
			throws Exception {
		Iterator<Entry<String, JsonNode>> iterator = jsonNode1.fields();
		Iterator<Entry<String, JsonNode>> iterator2 = jsonNode2.fields();
		boolean flag = true;
		int countContent1 = 0, countContent2 = 0;
		if (jsonNode1.size() != jsonNode2.size()) {

			flag = false;
		}
		while (flag && iterator.hasNext()) {
			Entry<String, JsonNode> entryset = iterator.next();
			String jsonKey = entryset.getKey();
			JsonNode valueJsonNode = entryset.getValue();
			Iterator<String> firstJsonFieldNames = valueJsonNode.fieldNames();
			int count = 0;
			while (iterator2.hasNext()) {
				Entry<String, JsonNode> entryset1 = iterator2.next();
				if (!jsonKey.equalsIgnoreCase(entryset1.getKey()))
					count++;
				if (count == 0) {
					Iterator<String> secondJsonFieldNamesList = entryset1.getValue().fieldNames();
					ArrayList<String> secondJsonFieldNames = new ArrayList<String>();
					while (secondJsonFieldNamesList.hasNext()) {
						secondJsonFieldNames.add(secondJsonFieldNamesList.next());
					}
					int countmatch = 0;
					int countData = 0, countvalueUnMatch = 0;
					while (firstJsonFieldNames.hasNext()) {
						String keyInside = firstJsonFieldNames.next();
						countData++;
						for (Object secondkeyInside : secondJsonFieldNames) {
							String strKey = (String) secondkeyInside;
							if (strKey.equalsIgnoreCase(keyInside)) {
								JsonNode jsonNodeFirst = entryset.getValue();
								JsonNode jsonNodeSecond = entryset1.getValue();
								if (jsonNodeFirst.has(strKey) && jsonNodeSecond.has(strKey)) {
									String firstValue = jsonNodeFirst.get(strKey).asText();
									String secondValue = jsonNodeSecond.get(strKey).asText();
									if (!firstValue.equalsIgnoreCase(secondValue)) {
										countvalueUnMatch++;
										break;
									} else
										countmatch++;
								}

							}
						}
						if (countvalueUnMatch > 0)
							break;
					}
					if (countmatch != countData) {

						flag = false;
					}
				}
			}
			if (count > 0) {

				flag = false;
			}
		}
		if (flag && (countContent1 != countContent2)) {

			flag = false;
		}
		return flag;
	}

}

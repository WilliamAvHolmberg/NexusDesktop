import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.json.JsonException;

public class Test {

	public static void main(String[] args) {
		new Test();
	}

	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	public Test() {

//		String sURL = "http://60.226.19.173:3000/proxies/140/json"; // just a string
//
//		JSONObject obj = null;
//		try {
//			obj = getJSONFromUrl(sURL);
//			System.out.println(obj);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

//		PrivateProxy proxy = null;
//		try {
//			JSONObject proxySettings = obj.getJSONObject("proxySettings");
//			proxy = new PrivateProxy(
//					proxySettings.getString("username"), proxySettings.getString("password"),
//					proxySettings.getString("host"), proxySettings.getString("port"));
//		}catch (JSONException je){ je.printStackTrace(); }

		// Test Code
		PrivateProxy proxy = new PrivateProxy("", "", "", "");
		proxy.host = "12.164.246.97";
		proxy.port = "20000";
		proxy.username = "craig343";
		proxy.password = "craig343";

//		AccountCreatorTest2 creator = new AccountCreatorTest2();
//		creator.createAccount("Will-I-Am", "notpewdiepie@youtube.com", "Sub2Pewdiepie", proxy, "");
		TempMailRestorer creator = new TempMailRestorer();
		creator.recoverAccount("Will-I-Am", "notpewdiepie@youtube.com", "Sub2Pewdiepie", proxy, "");

//		if(creator != null)
//			return;
//		String windowsProfilePath = System.getenv("APPDATA") + "\\Mozilla\\Firefox\\Profiles";
//		String profileName = getProfileName(windowsProfilePath);
//		if (profileName == null) {
//			return;
//		}
//		String jsonPath = windowsProfilePath + "\\" + profileName
//				+ "\\browser-extension-data\\{0c3ab5c8-57ac-4ad8-9dd1-ee331517884d}\\storage.js";
//		File jsonFile = new File(jsonPath);
//		System.out.println("Writing JSON object to file");
//		System.out.println("-----------------------");
//		try {
//			FileWriter fileWriter = new FileWriter(jsonFile);
//			fileWriter.write(obj.toString());
//			fileWriter.flush();
//			fileWriter.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void listFiles(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			System.out.println(name);
		}
	}

	public String getProfileName(String path) {
		// windows
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			if (name.contains(".default")) {
				return name;
			}
		}
		return null;
	}

	public JSONObject getJSONFromUrl(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// optional default is GET
		con.setRequestMethod("GET");
		// add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// print in String
		// Read JSON response and print
		return new JSONObject(response.toString());
	}

}

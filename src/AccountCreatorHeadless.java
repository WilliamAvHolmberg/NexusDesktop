

import com.anti_captcha.Api.NoCaptchaProxyless;
import com.anti_captcha.Helper.DebugHelper;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.medusa.Utils.Logger;
import org.medusa.Utils.SessionStorage;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AccountCreatorHeadless {

	//Strings which will contain account information 
	public static String emailDomain = "";
	public static String emailPrefix = "";
	public static String passwd = "";

	//Program version
	public static double version = 0.6;
	public static String v = "Alpha";


	//Custom name setting(s)
	public static boolean customUN = false;

	//Creation statistics and stuff
	public static int currentProgressive = 0;
	public static int currentNumber = 0;
	public static int accountsWanted = 1;
	public static boolean started = false;
	public static String antiCaptchaKey = "0ed783b4f28df5bbf166e3db012e412d";
	public static int accountsCreated = 0;
	public static int completeNumber = 0;
	
	//Site key
	public static String siteKey = "6Lcsv3oUAAAAAGFhlKrkRb029OHio098bbeyi_Hv";

	//Session Stuff
	public static SessionStorage st = new SessionStorage();
	private int attempts = 0;
	private String CAPTCHA_SOLVER = "anticaptcha";
	private final String RUNESCAPE_URL = "https://secure.runescape.com/m=account-creation/create_account";

	String getCaptcha(){
		int attempts = 0;
		boolean completed = false;
		String token = null;
		while (token == null) {
			if (attempts < 5) {
				switch (CAPTCHA_SOLVER) {
					case "anticaptcha":
						AntiCaptcha antiCaptcha = new AntiCaptcha();
						try {
							Logger.log("Waiting for captcha code... This might take a while...");
							token = antiCaptcha.solveCaptcha(RUNESCAPE_URL);
							completed = true;
						} catch (MalformedURLException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case "twocaptcha":
						// TODO: 2Captcha Support
						break;
				}
				attempts++;
			} else {
				System.out.println("Captcha Solver Failed 5 Times - Stopping");
				break;
			}
			if(token == null)
				try { Thread.sleep(2000); }catch (Exception ex){}
		}
		return token;
	}
	

	//Make request to solve captcha. If solved proceed to account creation.
	public void createAccount(String username, String email, String password, PrivateProxy proxy, String address) throws MalformedURLException, InterruptedException {
		if (proxy != null && proxy.host.length() > 5) {
			Logger.log("Connecting to Proxy " + proxy.host + ":" + proxy.port);
			if(!proxy.setSystemProxy())
				return;
			Logger.log("Successfully connected");
		}
		Logger.log("Waiting for captcha code... This might take a while...");
		String token = getCaptcha();

		if (token != null && token.length() > 5) {
				createProxyPost(username, email, password,token, proxy, address);
		}else {
			Logger.log("failed captcha");
		}
	}


	//Create account with proxy
	public void createProxyPost(String username, String email, String password, String gresponse, PrivateProxy proxy, String address) {
		{
			Logger.log("IN POST. " + gresponse);
			HttpClient httpclient = HttpClients.createDefault();
			try {

				HttpPost httppost = new HttpPost("https://secure.runescape.com/m=account-creation/create_account");


				
				Random rand = new Random();
				int day = (1 + rand.nextInt(29));
				int month = (1 + rand.nextInt(11));
				int year = (1965 + rand.nextInt(30));
				
				// Request parameters and other properties.
				List<NameValuePair> params = new ArrayList<NameValuePair>(2);
				params.add(new BasicNameValuePair("email1", email));
				params.add(new BasicNameValuePair("onlyOneEmail", "1"));
				params.add(new BasicNameValuePair("password1", password));
				params.add(new BasicNameValuePair("onlyOnePassword", "1"));
				params.add(new BasicNameValuePair("day", Integer.toString(day)));
				System.out.println("Day: " + day);
				params.add(new BasicNameValuePair("month", Integer.toString(month)));
				System.out.println("Month: " + month);
				params.add(new BasicNameValuePair("year", Integer.toString(year)));
				System.out.println("Year: " + year);
				params.add(new BasicNameValuePair("g-recaptcha-response", gresponse));
				params.add(new BasicNameValuePair("create-submit", "create"));
				httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
				System.out.println("Key: " + antiCaptchaKey);

				//Set headers
				httppost.setHeader("Host", "secure.runescape.com");
				httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0");
				httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
				httppost.setHeader("Accept-Language", "en-US,en);q=0.5");
				httppost.setHeader("Accept-Encoding", "gzip, deflate, br");
				httppost.setHeader("Referer", "http://oldschool.runescape.com/");

				//Execute and get the response.
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					InputStream instream = entity.getContent();
					String getResponseString = readStream(instream);

					completeNumber++;
					try {
						if(getResponseString.length() < 2) {
							Logger.log("no response!");
						}else {
							Logger.log(getResponseString);
						}
		
						if (getResponseString.contains("Account Created") || getResponseString.length() < 2){
							AccountRecover.createAccountUnlocked(email, password);
							AccountLauncher.launchClient(email,address);						} 
						else {
							if(attempts < 1) {
							Logger.log(getResponseString);
							Logger.log("Creation failed...lets try again. No error");
							attempts += 1;
							createProxyPost(username, email, password, gresponse, proxy, address);
							return;
							}else {
								createIPCooldownMessage(proxy.host, 120);
								Logger.log("OUT OF ATTEMPTS. SEND COOLDOWN");
							}
						}
					} finally {
						instream.close();
					}
				}
			} catch (IOException e) {
				completeNumber++;
				Logger.log("Failed to connect to proxy");
			}
		}
	}

	//Reader
	static String readStream(InputStream stream) throws IOException {
		try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = stream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			return result.toString("UTF-8");
		}
	}

	//Random alphanum String
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}
	
	public void createIPCooldownMessage(String host, int time) {
		Logger.log("CREATED BAD IP COOLDOWNMESS");
		NexHelper.messageQueue.push("ip_cooldown:" + host + ":" + time);
	}


}

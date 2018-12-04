

import com.anti_captcha.Api.NoCaptchaProxyless;
import com.anti_captcha.Helper.DebugHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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

public class CreateAccount {

	// Program version
	public static double version = 0.51;
	public static String v = "Alpha";

	// Proxy setting(s)
	public static boolean proxies = false;

	// Custom name setting(s)
	public static boolean customUN = false;

	// Creation statistics and stuff

	private String antiCaptchaKey = "01bd2a7839593708e019c567485f8c61";

	// Session Stuff
	public static SessionStorage st = new SessionStorage();
	private String email;
	private String username;
	private String password;
	private String ip;
	private String port;

	public CreateAccount(String email, String username, String password, String ip, String port) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.ip = ip;
		this.port = port;
		st.logger = true;

		// Prints text to logger
		Logger.log("Welcome to Medusa's Account Creator (v" + CreateAccount.version + "-" + CreateAccount.v + ")");
		Logger.log("Please note that this might not work 100% of the time");
	}

	// Make request to solve captcha. If solved proceed to account creation.
	public void createAccount() throws MalformedURLException, InterruptedException {
		Logger.log("Waiting for captcha code... This might take a while...");
		DebugHelper.setVerboseMode(false);
		NoCaptchaProxyless api = new NoCaptchaProxyless();
		api.setClientKey(antiCaptchaKey);
		api.setWebsiteUrl(new URL("https://secure.runescape.com/m=account-creation/create_account"));
		api.setWebsiteKey("6LccFA0TAAAAAHEwUJx_c1TfTBWMTAOIphwTtd1b");
		Logger.log("we are down here");
		if (!api.createTask()) {
			Logger.log(api.getErrorMessage());
		} else if (!api.waitForResult()) {
			Logger.log("-----------------------");
			Logger.log("Failed to solve captcha");
		} else {
			createPost(api.getTaskSolution().getGRecaptchaResponse());
			Logger.log("we are even down here");
		}
	}

	// Create Account without proxy
	public void createPost(String string) {
		HttpClient httpclient = HttpClients.createDefault();
		try {

			HttpPost httppost = new HttpPost("https://secure.runescape.com/m=account-creation/create_account");

			// Request parameters and other properties.
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("email1", email));
			params.add(new BasicNameValuePair("onlyOneEmail", "1"));
			params.add(new BasicNameValuePair("password1", password));
			params.add(new BasicNameValuePair("onlyOnePassword", "1"));
			params.add(new BasicNameValuePair("displayname", username));
			params.add(new BasicNameValuePair("day", "1"));
			params.add(new BasicNameValuePair("month", "2"));
			params.add(new BasicNameValuePair("year", "1999"));
			params.add(new BasicNameValuePair("g-recaptcha-response", string));
			params.add(new BasicNameValuePair("submit", "Play Now"));
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

			// Set headers
			httppost.setHeader("Host", "secure.runescape.com");
			httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0");
			httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			httppost.setHeader("Accept-Language", "en-US,en);q=0.5");
			httppost.setHeader("Accept-Encoding", "gzip, deflate, br");
			httppost.setHeader("Referer", "http://oldschool.runescape.com/");

			// Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String getResponseString = readStream(instream);

				try {
					Logger.log("-----------------------");
					Logger.log(email + ":" + password + ":" + username);
					if (getResponseString.contains("Account Created") || getResponseString.length() < 2) {
						String proxy1 = "-proxy " + " " + ":" + " " + ":" + " " + ":" + " ";

						AccountLauncher.launchClient("./osbot.jar", "NEX", "wavh", "Lifeosbotbook123", email,
								password, "301", proxy1, "asd");

						Logger.log("-----------------------");
						Logger.log("Task done");

					} else {
						Logger.log("Creation failed...");
						Logger.log("-----------------------");
						Logger.log("Task done");
					}
				} finally {
					instream.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Reader
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

	// Random alphanum String
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

}

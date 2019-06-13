package org.nex.unlocker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nex.unlocker.proxy.SocksProxy;

public class MailHandler {
	public static String recoverUrl = "https://secure.runescape.com/m=accountappeal/passwordrecovery";
	private SocksProxy proxy;
	private CaptchaSolver solver;
	
	public MailHandler(SocksProxy proxy, CaptchaSolver solver) {
		this.proxy = proxy;
		this.solver = solver;
	}

	public String getResetLink(String mailID) {

		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet get = new HttpGet("https://privatix-temp-mail-v1.p.rapidapi.com/request/source/id/" + mailID + "/");
		String intialURL = "https://secure.runescape.com/m=accountappeal/enter_security_code.ws?code=";
		System.out.println("ID:" + mailID);

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		get.setHeader("X-RapidAPI-Host", "privatix-temp-mail-v1.p.rapidapi.com");
		get.setHeader("X-RapidAPI-Key", "4c0e429fd6mshf2cbfeb2892c652p1f83bfjsn5f63eb9b0431");
		get.setHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			HttpResponse response = client.execute(get);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				if (line.toLowerCase().contains("enter_security_code".toLowerCase())) {
					String firstSplit = line.split("code=")[1];

					String secondSplit = firstSplit.split("reset")[0];
					int lengthOfSplit = secondSplit.length();
					String resetID = secondSplit.substring(0, lengthOfSplit - 3);
					String finalURL = intialURL + resetID;
					System.out.println("URL:" + finalURL);

					return finalURL;
				}
			}
			rd.close();
		} catch (ClientProtocolException e) {
			System.out.print(e.getMessage());
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
		return null;
	}

	public String getEmailID(String email){
		String md5 = DigestUtils.md5Hex(email.toLowerCase());
		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet get = new HttpGet("https://privatix-temp-mail-v1.p.rapidapi.com/request/mail/id/" + md5 + "/");

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		get.setHeader("X-RapidAPI-Host", "privatix-temp-mail-v1.p.rapidapi.com");
		get.setHeader("X-RapidAPI-Key", "4c0e429fd6mshf2cbfeb2892c652p1f83bfjsn5f63eb9b0431");
		get.setHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			HttpResponse response = client.execute(get);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder fullResponse = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				fullResponse.append(line);
			}
			rd.close();
		
			if (fullResponse.toString().contains("mail_id") && fullResponse.toString().toLowerCase().contains("Reset".toLowerCase())) {
				JSONArray array = new JSONArray(fullResponse.toString());
				for(int i = 0; i< array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					String subject = (String) object.get("mail_subject");
					String email_id = (String) object.get("mail_id");
					if(subject.toLowerCase().contains("reset")) {
						return email_id;
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isEmailAvailable(String email) {
		String domain = "@" + email.split("@")[1];
		List<String> availableDomains = getMailDomains();
		if (availableDomains.contains(domain.toLowerCase())) {
			return true;
		}
		return false;
	}

	public List<String> getMailDomains() {
		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet get = new HttpGet("http://nexus.myftp.biz:3000/accounts/available_mail_domains");

		// add header

		List<String> emails = new ArrayList<String>();
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		get.setHeader("Accept-Language", "en-US,en;q=0.8");
		get.setHeader("User-Agent", "Mozilla");
		get.setHeader("Referer", "http://oldschool.runescape.com");

		boolean linkFound = false;
		try {

			HttpResponse response = client.execute(get);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			StringBuilder emailsRespond = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				emailsRespond.append(line);
			}
			rd.close();

			String[] parsedEmails = emailsRespond.toString().replace("\"", "").replace("[", "").replace("]", "")
					.split(",");
			for (String email : parsedEmails) {
				emails.add(email.toLowerCase());
			}
		} catch (Exception e) {
		}
		return emails;
	}

	public String getRedirectURL(String email) {

		String recaptchaResponse = solver.getCaptchaReponse(proxy, recoverUrl);
		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost post = new HttpPost(recoverUrl);

		// add header

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		post.setHeader("Accept-Language", "en-US,en;q=0.8");
		post.setHeader("User-Agent", "Mozilla");
		post.setHeader("Referer", "http://oldschool.runescape.com");
		urlParameters.add(new BasicNameValuePair("email", email));
		urlParameters.add(new BasicNameValuePair("password-recovery-submit", "password-recovery"));
		urlParameters.add(new BasicNameValuePair("g-recaptcha-response", recaptchaResponse));
		String redirectUrl = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";

			while ((line = rd.readLine()) != null) {
				System.out.println(line);
				if (line.toLowerCase().contains("not being re")) {
					String firstSplit = line.split("<a href='")[1];
					String secondSplit = firstSplit.split("' data-test")[0];
					redirectUrl = secondSplit;
					System.out.println("RETURING REDIRECT URL:" + redirectUrl);
				}
			}

		} catch (Exception e) {
		}

		return redirectUrl;

	}

}

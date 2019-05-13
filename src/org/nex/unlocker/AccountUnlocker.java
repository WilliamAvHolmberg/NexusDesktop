package org.nex.unlocker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nex.unlocker.proxy.SocksProxy;



public class AccountUnlocker {

	private String apiKey, googleKey, pageUrl;
	private SocksProxy proxy;
	private CaptchaSolver solver;
	private MailHandler mailHandler;

	public AccountUnlocker(String apiKey, String googleKey, String pageUrl, SocksProxy proxy) {
		this.apiKey = apiKey;
		this.googleKey = googleKey;
		this.pageUrl = pageUrl;
		this.proxy = proxy;
		this.solver = new CaptchaSolver(apiKey, googleKey);
		this.mailHandler = new MailHandler(proxy,solver);
	}
	
	public boolean recoverAccount(String email) {
		String redirectUrl = mailHandler.getRedirectURL(email);
		if(redirectUrl != null && redirectUrl.length() > 5) {
			if(visitRecoverUrl(redirectUrl)) {
				return true;				
			}
		}
		return false;
	}
	
	
	
	public StatusMessage unlockAccount(String email) {
		System.out.println("Making sure email is available");
		if(!mailHandler.isEmailAvailable(email)) return StatusMessage.DOMAIN_NOT_FOUND;
		System.out.println("Email is available.");
		
		
		
		System.out.println("Activating proxy.");
		if(!proxy.setSystemProxy()) {
			System.out.println("Failed to activate proxy.");
			return StatusMessage.PROXY_FAILED;
		}
		System.out.println("Successfully set proxy.");
		
		
		System.out.println("Checking if we have received email");
		String mailID = mailHandler.getEmailID(email);
		
		if(mailID == null) {
			System.out.println("No email found. Starting account recover process");
			if(recoverAccount(email)) {
				System.out.println("Successfully recovered account. Lets wait 10 sec so we can receive mail.");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mailID = mailHandler.getEmailID(email);
				if(mailID == null) return StatusMessage.MAIL_NOT_FOUND;
			}else {
				return StatusMessage.RECOVERY_BLOCKED;
			}
		}
		
		System.out.println("Successfully found email.");
		System.out.println("Checking the specifik mail for reset link");

		String resetLink = mailHandler.getResetLink(mailID);
		
		if(resetLink == null) return StatusMessage.MAIL_NOT_FOUND;
		
		System.out.println("Visiting set password link.");
		String secondResetLink = getSecondResetLink(resetLink);
		
		if(secondResetLink == null) return StatusMessage.PASSWORD_BLOCKED;
		System.out.println("Settings new password");
		
		if(!setNewPassword(secondResetLink)) return StatusMessage.PASSWORD_BLOCKED;
		System.out.println("Successfully set new password. We are done.");
		
		
		return StatusMessage.COMPLETED;
	}
	
	private boolean setNewPassword(String resetLink) {

		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpPost post = new HttpPost(resetLink);
		
		// add header


		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		post.setHeader("Accept-Language", "en-US,en;q=0.8");
		post.setHeader("User-Agent", "Mozilla");
		post.setHeader("Referer","http://oldschool.runescape.com");
		urlParameters.add(new BasicNameValuePair("password", "ugot00wned3"));
		urlParameters.add(new BasicNameValuePair("confirm","ugot00wned3"));
		urlParameters.add(new BasicNameValuePair("submit", "Change+Password"));
		boolean linkFound = false;
		try {
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			
			while ((line = rd.readLine()) != null) {
				if(line.toLowerCase().contains("Your new password has been set and you may use it to log into your account".toLowerCase())) {
					linkFound = true;
				}
			}
			rd.close();
			
		} catch (Exception e) {
		}

		return linkFound;

	}
	
	private String getSecondResetLink(String resetURL) {
		
		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet get = new HttpGet(resetURL);
		HttpClientContext context = HttpClientContext.create();
		
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		get.setHeader("X-RapidAPI-Host", "privatix-temp-mail-v1.p.rapidapi.com");
		get.setHeader("X-RapidAPI-Key", "4c0e429fd6mshf2cbfeb2892c652p1f83bfjsn5f63eb9b0431");
		get.setHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			HttpResponse response = client.execute(get, context);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			
			URI finalUrl = get.getURI();
			List<URI> locations = context.getRedirectLocations();
			if (locations != null) {
			    finalUrl = locations.get(locations.size() - 1);
			    return finalUrl.toString();
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public boolean visitRecoverUrl(String url) {
		HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet get = new HttpGet(url);
		
		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		get.setHeader("Accept-Language", "en-US,en;q=0.8");
		get.setHeader("User-Agent", "Mozilla");
		get.setHeader("Referer","http://oldschool.runescape.com");
		boolean recoveredAccount = false;
		try {
			HttpResponse response = client.execute(get);
			System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				if(line.toLowerCase().contains("Check your email for a link to recover your account".toLowerCase())) {
					recoveredAccount = true;
				}	
			}
			rd.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recoveredAccount;
	}

	

	

}

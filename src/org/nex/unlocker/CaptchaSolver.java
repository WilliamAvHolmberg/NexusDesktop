package org.nex.unlocker;

import java.io.IOException;
import java.lang.reflect.Proxy;

import org.nex.unlocker.proxy.SocksProxy;

import com.twocaptcha.api.TwoCaptchaService;

public class CaptchaSolver {
	private String apiKey, googleKey;
	public CaptchaSolver(String apiKey, String googleKey) {
		this.apiKey = apiKey;
		this.googleKey = googleKey;
	}
	public String getCaptchaReponse(SocksProxy proxy, String pageUrl) {
		/**
		 * With proxy and user authentication
		 */
		TwoCaptchaService service = new TwoCaptchaService(apiKey, googleKey, pageUrl, proxy.getIP(),
				proxy.getPassword(), proxy.getUsername(), proxy.getPassword(), proxy.getType());

		/**
		 * Without proxy and user authentication TwoCaptchaService service = new
		 * TwoCaptchaService(apiKey, googleKey, pageUrl);
		 */

		try {
			String responseToken = service.solveCaptcha();
			System.out.println("The response token is: " + responseToken);
			return responseToken;
		} catch (InterruptedException e) {
			System.out.println("ERROR case 1");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR case 2");
			e.printStackTrace();
		}
		return null;
	}
}

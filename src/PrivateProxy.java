import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import org.medusa.Utils.Logger;

public class PrivateProxy {
	public String username;
	public String password;
	public String host;
	public String port;

	public PrivateProxy(String username, String password, String host, String port) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	public boolean setSystemProxy() {
		if (host.length() < 5)
			return true;

		String proxySet = host.length() > 6 ? "true" : "false";
		System.getProperties().put("proxySet", proxySet);
		System.getProperties().put("socksProxyHost", host);
		System.getProperties().put("socksProxyPort", port);
		Authenticator.setDefault(new ProxyAuth(username, password));
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			
			String ip = in.readLine(); // you get the IP as a String
			Logger.log("IP IS GOOD?" + ip);
			return true;
		} catch (IOException e) {
			System.out.println("Proxy " + host + " is bad");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
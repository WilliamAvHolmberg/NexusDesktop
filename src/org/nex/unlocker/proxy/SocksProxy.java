package org.nex.unlocker.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;

import com.twocaptcha.api.ProxyType;




public class SocksProxy {
	
	private String ip;
	private String port;
	private String username;
	private String password;
	private ProxyType type;
	public SocksProxy(String ip, String port, String username, String password, ProxyType type) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.type = type;
	}
	
	public String getIP() {
		return ip;
	}
	
	public String getPort() {
		return port;
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}

	public ProxyType getType() {
		return type;
	}
	
	public boolean setSystemProxy() {
		if (ip.length() < 5)
			return true;

		System.getProperties().put("proxySet", "true");
		System.getProperties().put("socksProxyHost", ip);
		System.getProperties().put("socksProxyPort", port);
		Authenticator.setDefault(new ProxyAuth(username, password));
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			
			String newIp = in.readLine(); // you get the IP as a String
			System.out.println("IP SHOULD BE:" + ip +" .....and is:" + newIp);
			if(newIp.equals(ip)) return true;
		} catch (IOException e) {
			System.out.println("Proxy " + ip + " is bad");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}

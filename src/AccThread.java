import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.medusa.Utils.Logger;

public class AccThread implements Runnable{

	String username;
	String email;
	String password;
	Proxy proxy;
	public AccThread(String username, String email, String password, Proxy proxy) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.proxy = proxy;
	}
	@Override
	public void run() {
		CreateAccount ca = new CreateAccount();
		Logger.log("Trying to create acc");
		try {
			ca.createAccount(username, email, password, proxy);
			URL whatismyip;
			String ip = "";
			try {
				whatismyip = new URL("http://checkip.amazonaws.com");
				BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

				ip = in.readLine(); // you get the IP as a String
				System.out.println(ip);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Logger.log("we created acc with ip:" + ip + "   should have created with: " + proxy.host );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.log("finished with trying to create acc");
	}

}

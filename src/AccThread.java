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
	PrivateProxy proxy;
	String address;
	public AccThread(String username, String email, String password, PrivateProxy proxy, String address) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.proxy = proxy;
		this.address = address;
	}
	@Override
	public void run() {
		Logger.log("Trying to create acc");
		AccountCreator ac = new AccountCreator();
		ac.createAccount(username, email, password, proxy, address);
		Logger.log("finished with trying to create acc");
	}

}

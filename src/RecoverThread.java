import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.medusa.Utils.Logger;

public class RecoverThread implements Runnable{

	String username;
	String email;
	String password;
	PrivateProxy proxy;
	String address;
	public RecoverThread(String username, String email, String password, PrivateProxy proxy, String address) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.proxy = proxy;
		this.address = address;
	}
	@Override
	public void run() {
		try {
			Logger.log("Trying to create acc");
			AccountRecover recover = new AccountRecover(proxy, email, password, address);
			Logger.log("finished with trying to create acc");
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

}

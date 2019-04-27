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
	public static boolean CAN_RUN = true;
	public AccThread(String username, String email, String password, PrivateProxy proxy, String address) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.proxy = proxy;
		this.address = address;
	}
	@Override
	public void run() {
		try {
			while(!CAN_RUN) {
				Logger.log("Waiting for acc thread to be ready.");
				Thread.sleep(5000);
			}
			CAN_RUN = false;
			Logger.log("Trying to create acc");
			AccountCreator ac = new AccountCreator();
			ac.createAccount(username, email, password, proxy, address);
			Logger.log("finished with trying to create acc");
			CAN_RUN = true;
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

}

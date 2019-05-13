import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.medusa.Utils.Logger;
import org.nex.unlocker.AccountUnlocker;
import org.nex.unlocker.StatusMessage;
import org.nex.unlocker.proxy.SocksProxy;

public class RecoverThread implements Runnable{
	String googleKey = "6Lcsv3oUAAAAAGFhlKrkRb029OHio098bbeyi_Hv";
	String runescapeRecoverUrl = "https://secure.runescape.com/m=accountappeal/passwordrecovery";
	String twoCaptchaKey= "e31776b8685b07026204462c8919564e";
	String username;
	String email;
	String password;
	SocksProxy proxy;
	String address;
	public RecoverThread(String username, String email, String password, SocksProxy proxy, String address) {
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
			AccountUnlocker unlocker = new AccountUnlocker(twoCaptchaKey, googleKey, runescapeRecoverUrl, proxy);
			StatusMessage message = unlocker.unlockAccount(email);
			switch(message) {
			case COMPLETED:
				createAccountUnlocked(email, "ugot00wned3");
				break;
			case RECOVERY_BLOCKED:
			case PASSWORD_BLOCKED:
				createUnlockCooldownMessage(proxy.getIP(), 600);
				break;
			case DOMAIN_NOT_FOUND:
				Logger.log("EMAIL DOMAIN NOT FOUND.");
				break;
			case MAIL_NOT_FOUND:
				Logger.log("MAIL NOT FOUND");
				break;
			case PROXY_FAILED:
				Logger.log("BAD PROXY");
				break;
			default:
				break;
			
			}
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void createAccountUnlocked(String email, String newPassword) {
		Logger.log("CREATED UNLOCKED MESSAGE");
		NexHelper.messageQueue.push("unlocked_account:" + email + ":" + newPassword);
	}

	public static void createUnlockCooldownMessage(String host, int time) {
		Logger.log("CREATED unlock COOLDOWNMESS");
		NexHelper.messageQueue.push("unlock_cooldown:" + host + ":" + time);
	}

}

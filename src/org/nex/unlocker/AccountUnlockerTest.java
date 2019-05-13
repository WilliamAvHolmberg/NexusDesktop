package org.nex.unlocker;

import java.io.IOException;

import org.nex.unlocker.proxy.SocksProxy;

import com.twocaptcha.api.ProxyType;

public class AccountUnlockerTest {
	String googleKey = "6Lcsv3oUAAAAAGFhlKrkRb029OHio098bbeyi_Hv";
	String runescapeRecoverUrl = "https://secure.runescape.com/m=accountappeal/passwordrecovery";
	String twoCaptchaKey= "e31776b8685b07026204462c8919564e";
	public static void main(String[]args) {
		new AccountUnlockerTest();
	}
	
	public AccountUnlockerTest() {
		SocksProxy proxy = new SocksProxy("50.239.137.52", "20000", "william50", "william50", ProxyType.SOCKS5);
		AccountUnlocker unlocker = new AccountUnlocker(twoCaptchaKey, googleKey, runescapeRecoverUrl, proxy);
		//unlocker.recoverAccount("boarpittgu@simpleemail.info");
		System.out.println(unlocker.unlockAccount("MagiAdaS@key-mail.net"));
		
	}

}

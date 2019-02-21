
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.medusa.Utils.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountCreatorTest2 {

	private static final String RUNESCAPE_URL = "https://secure.runescape.com/m=account-creation/create_account";
	private static final String RANDGEN_URL = "https://randomuser.me/api/?nat=gb";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
	private static String CAPTCHA_SOLVER = "anticaptcha";
	//private static String token = null;

	boolean setProxy(PrivateProxy currentProxy) {

		String proxySet = currentProxy.host.length() > 6 ? "true" : "false";
		System.getProperties().put("proxySet", proxySet);
		System.getProperties().put("socksProxyHost", currentProxy.host);
		System.getProperties().put("socksProxyPort", currentProxy.port);
		Authenticator.setDefault(new ProxyAuth(currentProxy.username, currentProxy.password));
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			String ip = in.readLine(); // you get the IP as a String
			System.out.println(ip);
			return true;
		} catch (IOException e) {
			System.out.println("Proxy " + currentProxy.host + " is bad");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public class ProxyAuth extends Authenticator {
		private PasswordAuthentication auth;

		private ProxyAuth(String user, String password) {
			auth = new PasswordAuthentication(user, password == null ? new char[] {} : password.toCharArray());
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return auth;
		}
	}

	public boolean createAccount(String username, String email, String password, PrivateProxy proxy, String address) {
		if (proxy != null && proxy.host.length() > 5) {
			Logger.log("Connecting to Proxy " + proxy.host + ":" + proxy.port);
			if(!setProxy(proxy))
				return false;
			Logger.log("Successfully connected");
		}
		int attempts = 0;
		boolean completed = true;
		String token = "CAPTCHA_TOKEN";
		if (completed) {
			try {
				postForm(token, username, email, password, proxy, address);
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			System.out.println("Couldnt get captcha :(");
		}
		return false;
	}

	private static void waitForLoad(WebDriver driver) {
		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(pageLoadCondition);
	}

	private static void setFirefoxDriver() {
		ClassLoader classLoader = AccountCreator.class.getClassLoader();
		URL resource = classLoader.getResource("drivers/" + getDriverNameFirefox());
		//System.out.println(resource);
		File f = new File("Driver");
		if (!f.exists()) {
			f.mkdirs();
		}
		File driver = new File("Driver" + File.separator + getDriverNameFirefox());
		if (!driver.exists()) {
			try {
				driver.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.setExecutable(true);
			try {
				org.apache.commons.io.FileUtils.copyURLToFile(resource, driver);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.setProperty("webdriver.gecko.driver", driver.getAbsolutePath());
		// driver = new ChromeDriver();
		System.setProperty("webdriver.gecko.driver", driver.getAbsolutePath());
	}
	
	public static void createProfile(FirefoxProfile prfile, PrivateProxy proxy)
			throws IOException {
		
	
//		File background = new File(	System.getenv("ROAMING") + "\\Mozilla\\Firefox\\Profiles\\jf4mx129.SeleniumFF");
//		if (!background.exists()) {
//			try {
//				background.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			background.setExecutable(true);
//			try {
//				org.apache.commons.io.FileUtils.copyURLToFile(resource, background);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		URL resource2 = classLoader.getResource("manifest.json");
//		System.out.println(resource);
//		File driver = new File("manifest.json");
//		if (!driver.exists()) {
//			try {
//				driver.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			driver.setExecutable(true);
//			try {
//				org.apache.commons.io.FileUtils.copyURLToFile(resource2, driver);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		//background js that will be dynamic for us
//		Charset charset = StandardCharsets.UTF_8;
//		//read js file and replace proxy details
//		String content = new String(Files.readAllBytes(background.toPath()), charset);
//		content = content.replaceAll("proxy_type", "http");
//		content = content.replaceAll("proxy_host", proxy.host);
//		content = content.replaceAll("proxy_port", "10000");
//		content = content.replaceAll("proxy_username", proxy.username);
//		content = content.replaceAll("proxy_password", proxy.password);
//
//		//add modified background.js to zip
//		ZipEntry ze = new ZipEntry(background.getPath());
//		zout.putNextEntry(ze);
//		zout.write(content.getBytes(), 0, content.getBytes().length);
//		zout.closeEntry();
//
//
//		//add manifest to zip
//		File manifest = new File("manifest.json");
//		ZipEntry zeManifest = new ZipEntry(manifest.getPath());
//		zout.putNextEntry(zeManifest);
//		byte[] bytes = Files.readAllBytes(manifest.toPath());
//		zout.write(bytes, 0, bytes.length);
//		zout.closeEntry();
//		zout.close();
	}

	static FirefoxProfile copyProfileData(FirefoxProfile profile, PrivateProxy proxy){
		try
		{
			Field profileFolderVal = profile.getClass().getDeclaredField("model");
			profileFolderVal.setAccessible(true);
			File profileFolder = (File)profileFolderVal.get(profile);
			File extensionsDir = new File(AccountLauncher.curDir(), "extension");
			File localProfileFolder = new File(extensionsDir.toString(), "profile");

			File[] files = extensionsDir.listFiles((dir1, name) -> name.endsWith(".xpi"));
			for (File file : files)
				profile.addExtension(file);

			try {
				FileUtils.copyDirectory(localProfileFolder, profileFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Path proxySwitchSett = Paths.get(profileFolder.getPath(), "browser-extension-data/{0c3ab5c8-57ac-4ad8-9dd1-ee331517884d}/storage.js");
			if(Files.exists(proxySwitchSett)){
				Charset charset = StandardCharsets.UTF_8;
				String content = new String(Files.readAllBytes(proxySwitchSett), charset);
				content = content.replaceAll("%HOST%", proxy.host)
						.replaceAll("%PORT%", proxy.port)
						.replaceAll("%USERNAME%", proxy.username)
						.replaceAll("%PASSWORD%", proxy.password);
				Files.write(proxySwitchSett, content.getBytes(charset));
			}

			profile.setPreference("extensions.pendingOperations", true);
			profile.setPreference("services.sync.globalScore", 606);
		} catch (Exception ex){ex.printStackTrace(); return null;}
		return profile;
	}

	private static void postForm(String gresponse, String username, String loginEmail, String loginPassword,
			PrivateProxy proxy, String address) throws Exception {
		// ChromeOptions options = new ChromeOptions();
		// setting headless mode to true.. so there isn't any ui
		// options.setHeadless(true);

		// Create a new instance of the Chrome driver
		// WebDriver driver = new ChromeDriver(options);

		/*
		 * System.out.println("lets get path"); FirefoxOptions options = new
		 * FirefoxOptions(); FirefoxProfile profile = new FirefoxProfile();
		 * profile.setPreference("network.proxy.type", 1);
		 * profile.setPreference("network.proxy.socks", proxy.host);
		 * profile.setPreference("network.proxy.socks_port", proxy.port);
		 * 
		 * options.setProfile(profile);
		 */
		setFirefoxDriver();
		
		FirefoxProfile profile;
		FirefoxOptions options;
		if (proxy.username != null || proxy.username.length() > 0) {
			ProfilesIni ini = new ProfilesIni();
			profile = ini.getProfile("default");
			options = new FirefoxOptions();
			profile = copyProfileData(profile, proxy);
		} else {
			options = new FirefoxOptions();
			profile = new FirefoxProfile();
			profile.setPreference("network.proxy.type", 1);
			profile.setPreference("network.proxy.socks", proxy.host);
			profile.setPreference("network.proxy.socks_port", proxy.port);
		}
		options.setProfile(profile);
		WebDriver driver = new FirefoxDriver(options);
		try {
			driver.manage().window().maximize();

			driver.get("http://ipchicken.com/");
			Logger.log("Waiting for Page Load...");
			waitForLoad(driver);

			while (driver != null)
				TimeUnit.SECONDS.sleep(1);

		}finally {
			try {
				if (driver != null)
					driver.quit();
			} catch (Exception e){ }
		}
	}

	private static String getDriverNameFirefox() {
		switch (AccountLauncher.getOperatingSystemType()) {
		case Linux:
			return "geckodriver_linux";
		case MacOS:
			return "geckodriver";
		case Windows:
			return "geckodriver.exe";
		default:
			break;

		}
		return null;
	}

}
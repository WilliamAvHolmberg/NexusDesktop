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
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.apache.commons.exec.util.StringUtils;
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

public class AccountCreator {

	private static final String RUNESCAPE_URL = "https://secure.runescape.com/m=account-creation/create_account";
	private static final String RANDGEN_URL = "https://randomuser.me/api/?nat=gb";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
	private static String CAPTCHA_SOLVER = "anticaptcha";
	//private static String token = null;
	private static int cooldown = 90;
	public static final boolean CAPTCHA_FIRST = false;


	public boolean createAccount(String username, String email, String password, PrivateProxy proxy, String address) {
		Logger.log("Waiting for captcha code... This might take a while...");
		if (proxy != null && proxy.host.length() > 5) {
			Logger.log("Connecting to Proxy " + proxy.host + ":" + proxy.port);
			if(!proxy.setSystemProxy())
				return false;
			Logger.log("Successfully connected");
		}
		String token = null;
		if(CAPTCHA_FIRST)
			token = getCaptcha();
		try {
			postForm(token, username, email, password, proxy, address);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	static String getCaptcha(){
		int attempts = 0;
		boolean completed = false;
		String token = null;
		while (token == null) {
			if (attempts < 5) {
				switch (CAPTCHA_SOLVER) {
					case "anticaptcha":
						AntiCaptcha antiCaptcha = new AntiCaptcha();
						try {
							token = antiCaptcha.solveCaptcha(RUNESCAPE_URL);
							completed = true;
						} catch (MalformedURLException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case "twocaptcha":
						// TODO: 2Captcha Support
						break;
				}
				attempts++;
			} else {
				System.out.println("Captcha Solver Failed 5 Times - Stopping");
				break;
			}
			if(token == null)
				try { Thread.sleep(2000); }catch (Exception ex){}
		}
		return token;
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
	
	static FirefoxProfile copyProfileData(FirefoxProfile profile, PrivateProxy proxy){
		try
		{
			System.out.println("CurDir: " + AccountLauncher.curDir());
			Field profileFolderVal = profile.getClass().getDeclaredField("model");
			profileFolderVal.setAccessible(true);
			File profileFolder = (File)profileFolderVal.get(profile);
			File extensionsDir = new File(AccountLauncher.curDir(),"extension");
			File localProfileFolder = new File(extensionsDir.toString(), "profile");

			File[] files = extensionsDir.listFiles((dir1, name) -> name.endsWith(".xpi"));
			for (File file : files)
				profile.addExtension(file);

			System.out.println("Copying profile data to: " + profileFolder);
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

	static boolean isNullOrEmpty(String str){
		if(str == null) return  true;
		if(str.trim().isEmpty()) return true;
		return false;
	}

	static synchronized void killLeftoverProcesses(){
		if (activeAccountCreators == 0){
			if (AccountLauncher.getOperatingSystemType() == AccountLauncher.OSType.Windows){
				Runtime rt = Runtime.getRuntime();
				final String[] processes = new String[] { "firefox.exe", "geckodriver.exe" };
				for(String process : processes) {
					try {
						rt.exec("taskkill /F /IM " + process);
					}catch (IOException e) {}
				}
			} else {
//				rt.exec("kill -9 " + ....);
			}
		}
	}

	static int activeAccountCreators = 0;
	public static void postForm(String gresponse, String username, String loginEmail, String loginPassword,
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

		killLeftoverProcesses();

		activeAccountCreators++;
		setFirefoxDriver();
		
		FirefoxProfile profile;
		FirefoxOptions options;
//		if (proxy.host != null && proxy.host.length() > 3 &&
//			proxy.username != null && proxy.username.length() > 3) {
			ProfilesIni ini = new ProfilesIni();
			profile = ini.getProfile("default");
			options = new FirefoxOptions();
			profile = copyProfileData(profile, proxy);
//		}
//		else {
//			options = new FirefoxOptions();
//			profile = new FirefoxProfile();
//			profile.setPreference("network.proxy.type", 1);
//			profile.setPreference("network.proxy.socks", proxy.host);
//			if(proxy.port.length() > 1)
//				profile.setPreference("network.proxy.socks_port", Integer.parseInt(proxy.port));
//		}
		options.setProfile(profile);
		WebDriver driver = null;
		boolean failed = false;
		try {
		 	driver = new FirefoxDriver(options);
			driver.manage().window().maximize();

			if(!ipIsRight(driver, proxy.host)) { //check if the proxy is actually set
				return;
			}
			
			boolean created = false;
			boolean captchaFailed = false;
			int attempts = 0;
			while (!failed && !created && !captchaFailed && attempts < 3) {
				if(gresponse == null) {
					gresponse = getCaptcha();
					if(gresponse == null) {
						System.out.println("Couldnt get captcha :(");
						return;
					}
				}

				attempts++;
				driver.get(RUNESCAPE_URL);
				Logger.log("Waiting for Page Load...");
				waitForLoad(driver);
				TimeUnit.SECONDS.sleep(1);

				WebElement dobDay = null, dobMonth = null, dobYear = null, email = null, password = null, textarea = null, submit = null;
				WebElement acceptCookies = null;
				for (int i = 0; i < 3; i++) {
					Logger.log("Page Loaded");
					try {
						dobDay = driver.findElement(By.name("day"));
						dobMonth = driver.findElement(By.name("month"));
						dobYear = driver.findElement(By.name("year"));
						email = driver.findElement(By.name("email1"));
						// WebElement displayname = driver.findElement(By.name("displayname"));
						password = driver.findElement(By.name("password1"));
						textarea = driver.findElement(By.id("g-recaptcha-response"));
						submit = driver.findElement(By.id("create-submit"));
						acceptCookies = ((FirefoxDriver) driver).findElementByClassName("c-cookie-consent__dismiss");
					}catch (Exception ex) {
						Logger.log("ERROR MESSAGEE");
						Logger.log("ERROR MESSAGEE");
						Logger.log("ERROR MESSAGEE");
						Logger.log("ERROR MESSAGEE");
						Logger.log("ERROR MESSAGEE");
						Logger.log("ERROR MESSAGEE");
						Logger.log("ERROR MESSAGEE");
						cooldown = 60;
						createIPCooldownMessage(proxy.host, cooldown);
						
					}
					if (dobDay == null || dobMonth == null || dobYear == null || email == null || password == null || textarea == null || submit == null) {
						TimeUnit.MILLISECONDS.sleep(2000);
						continue;
					}
					break;
				}

				Random r = new Random();
				String year = (1980 + (int)(r.nextDouble() * 20)) + "";
				String month = (1 + (int)(r.nextDouble() * 10)) + "";
				String day = (1 + (int)(r.nextDouble() * 26)) + "";
				Logger.log("Elements found");
				dobDay.sendKeys(day);
				dobMonth.sendKeys(month);
				dobYear.sendKeys(year);
				email.sendKeys(loginEmail);
				// displayname.sendKeys("williamsosos");
				password.sendKeys(loginPassword);
				try {
					if(acceptCookies != null)
						acceptCookies.click();
				} catch (Exception ex){}

				Logger.log("Form filled");
				JavascriptExecutor jse = (JavascriptExecutor) driver;

				TimeUnit.SECONDS.sleep(2);

				jse.executeScript("arguments[0].style.display = 'block';", textarea);
				if(gresponse != null && textarea != null) {
					Logger.log("Filled in g-recaptcha-response text-area");
					textarea.sendKeys(gresponse);
				}else{
					Logger.log("Could not find g-recaptcha-response text-area");
				}

				Logger.log("Scrolling");
				driver.switchTo().defaultContent();
				jse.executeScript("window.scrollBy(0,250)", "");
				TimeUnit.SECONDS.sleep(6);
				jse.executeScript("onSubmit()");
				//submit.sendKeys(Keys.ENTER);
				//submit.click();
				TimeUnit.SECONDS.sleep(6);	//added this for leaving the captcha too fast
				waitForLoad(driver);
				TimeUnit.SECONDS.sleep(10);	//added this for leaving the captcha too fast

				Logger.log("Opening Captcha");

				for(int i = 0; i < 5; i++) {
					TimeUnit.SECONDS.sleep(1);
					waitForLoad(driver);
					if (driver.findElements(By.id("p-create-error")).size() != 0) {
						Logger.log("Errooororo. lets send message timeout 10min");
						createIPCooldownMessage(proxy.host, 120);
						TimeUnit.SECONDS.sleep(3);
						failed = true;
						break;
					}else if (driver.findElements(By.className("m-character-name-alts__name")).size() != 0) {
						System.out.println("Username In Use - Trying another");
						WebElement newUsername = driver.findElement(By.className("m-character-name-alts__name"));
						newUsername.click();
						waitForLoad(driver);
						// submit.sendKeys(Keys.ENTER);
						TimeUnit.SECONDS.sleep(3);
						failed = true;
					} else if (driver.findElements(By.className("google-recaptcha-error")).size() != 0) {
						Logger.log("Google Recaptcha Error");
						captchaFailed = true;
						failed = true;
					} else if (driver.findElements(By.id("p-account-created")).size() != 0) {
						created = true;
						System.out.println("Account Created");
						String parsedProxy = "-proxy " + proxy.host + ":" + proxy.port + ":" + proxy.username + ":"
								+ proxy.password;
						AccountRecover.createAccountUnlocked(loginEmail, loginPassword);
						AccountLauncher.launchClient(username, address);
					}
					if(created || failed)
						break;
				}
				if (!created && !failed) {
					gresponse = null;
					continue;
				}
			}
		} finally {
			activeAccountCreators--;
			try {
				if (driver != null)
					driver.quit();
			} catch (Exception e){ }
			driver = null;
		}
		
	}

	private static boolean ipIsRight(WebDriver driver, String host) {
		if(host == null || host.length() < 5) return true;
		driver.get("http://ipv4.plain-text-ip.com/");
		waitForLoad(driver);
		try {
			AccountRecover.sleepUntilFindElement(driver, By.tagName("body"), 60);
		}catch (InterruptedException e){}
		String myIP = driver.findElement(By.tagName("body")).getText();
		if(myIP.contains("Error")) {
			Logger.log("IP Service Error - Continuing anyway");
			Logger.log("curr ip: " + myIP);
			Logger.log("ip that should be: " + host);
			return true;
		} else if(!myIP.contains(host)) {
			Logger.log("BAD IP. RETURN");
			Logger.log("curr ip: " + myIP);
			Logger.log("ip that should be: " + host);
			return false;
		}
		return true;
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
	
	public static void createIPCooldownMessage(String host, int time) {
		Logger.log("CREATED BAD IP COOLDOWNMESS");
		NexHelper.messageQueue.push("ip_cooldown:" + host + ":" + time);
	}

}
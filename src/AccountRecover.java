import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.medusa.Utils.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountRecover {

	private static final String RECOVERY_URL = "https://secure.runescape.com/m=accountappeal/passwordrecovery";
	private static final String EMAIL_SET_URL = "https://temp-mail.org/en/option/change/";
	private static final String EMAIL_REFRESH_URL = "https://temp-mail.org/en/option/refresh/";

	private static String SET_PASSWORD_URL = null;
	private static String OUR_MAIL_LINK = null;

	private static final String RANDGEN_URL = "https://randomuser.me/api/?nat=gb";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
	private static String CAPTCHA_SOLVER = "anticaptcha";
	// private static String token = null;
	private static int cooldown = 90;
	public static String newPassword = "ugot00wned3"; //hardcoded :)



	public AccountRecover(PrivateProxy proxy, String username, String password) throws InterruptedException {
		String gResponse = getCaptchaResponse();
		if (gResponse != null) {
			logIntoRunescape(gResponse, proxy, username, password);
		}else {
		 	Logger.log("Response == null");
		 	return;
		}
		FirefoxProfile profile;
		FirefoxOptions options;
		if (proxy.username != null && proxy.username.length() > 3) {
			ProfilesIni ini = new ProfilesIni();
			profile = ini.getProfile("default");
			options = new FirefoxOptions();
			profile = copyProfileData(profile, proxy);
		} else {
			options = new FirefoxOptions();
			profile = new FirefoxProfile();
			profile.setPreference("network.proxy.type", 1);
			profile.setPreference("network.proxy.socks", proxy.host);
			profile.setPreference("network.proxy.socks_port", Integer.parseInt(proxy.port));
		}
		options.setProfile(profile);
		WebDriver driver = new FirefoxDriver(options);
		if (doEmailLogin(driver, "sojusopa@yourweb.email")) {
			doEmailCheck(driver);
		}
		if (OUR_MAIL_LINK != null) {
			Logger.log(OUR_MAIL_LINK);
			getPasswordLink(driver);
		}

		if (SET_PASSWORD_URL != null) {
			setNewPassword(driver, newPassword);
		}
		Logger.log("not logged in");
	}

	public static boolean setNewPassword(WebDriver driver, String ourPassword) {
		try {
			driver.get(SET_PASSWORD_URL);
			Logger.log("Waiting for Page Load...");
			waitForLoad(driver);
			if (driver.findElements(By.id("p-account-recovery-reset-password")).size() == 0) {
				Logger.log("Did not load page. lets try again");
				setNewPassword(driver, ourPassword);
			}
			WebElement password = driver.findElement(By.name("password"));
			WebElement confirmPassword = driver.findElement(By.name("confirm"));
			WebElement submitButton = driver.findElement(By.name("submit"));
			// send recovery
			if (password != null) {
				password.sendKeys(ourPassword);
				confirmPassword.sendKeys(ourPassword);
				TimeUnit.SECONDS.sleep(6);
				Logger.log("Form filled");
				submitButton.click();
				TimeUnit.SECONDS.sleep(6); // added this for leaving the captcha too fast
				waitForLoad(driver);
				TimeUnit.SECONDS.sleep(20); // added this for leaving the captcha too fast
			} else {
				Logger.log("Page failed to load..");
			}

			// check if recovery was successful
			if (driver.findElements(By.id("p-account-recovery-tracking-result")).size() != 0) {
				Logger.log("Successfully set password");
				Logger.log("lets check mail");
				//send message - account is unlocked
			} else {
				Logger.log("something went wrong");
				//send message - account is not unlocked
				
			}
		} catch (Exception e) {
			Logger.log("we failed");
		}
		return true;
	}

	public static boolean getPasswordLink(WebDriver driver) {
		driver.get(OUR_MAIL_LINK);
		Logger.log("Waiting for Page Load...");
		waitForLoad(driver);

		List<WebElement> elements = driver.findElements(By.tagName("a"));
		String setPasswordUrl = null;
		for (WebElement element : elements) {
			String text = element.getText();
			Logger.log(text);
			if (text.contains("RESET PASSWORD")) {
				Logger.log("found mess");
				setPasswordUrl = element.getAttribute("href");
			}
		}
		SET_PASSWORD_URL = setPasswordUrl;
		return true;
	}

	public void logIntoRunescape(String gResponse, PrivateProxy proxy, String username, String password) {
		 setFirefoxDriver();
		// hardcoded

		FirefoxProfile profile;
		FirefoxOptions options;
		if (proxy.username != null && proxy.username.length() > 3) {
			ProfilesIni ini = new ProfilesIni();
			profile = ini.getProfile("default");
			options = new FirefoxOptions();
			profile = copyProfileData(profile, proxy);
		} else {
			options = new FirefoxOptions();
			profile = new FirefoxProfile();
			profile.setPreference("network.proxy.type", 1);
			profile.setPreference("network.proxy.socks", proxy.host);
			profile.setPreference("network.proxy.socks_port", Integer.parseInt(proxy.port));
		}
		options.setProfile(profile);
		WebDriver driver = new FirefoxDriver(options);
		boolean failed = false;
		try {
			driver.manage().window().maximize();

			if (!ipIsRight(driver, proxy.host)) { // check if the proxy is actually set
				return;
			}

			driver.get(RECOVERY_URL);
			Logger.log("Waiting for Page Load...");
			waitForLoad(driver);
			Logger.log("Site loaded.. Lets fill in username details");
			WebElement formUsername = driver.findElement(By.name("email"));
			WebElement textarea = driver.findElement(By.id("g-recaptcha-response"));

			// send recovery
			if (formUsername != null) {
				formUsername.sendKeys(username);
				TimeUnit.SECONDS.sleep(6);
				Logger.log("Form filled");
				JavascriptExecutor jse = (JavascriptExecutor) driver;

				jse.executeScript("arguments[0].style.display = 'block';", textarea);
				if (gResponse != null && textarea != null) {
					Logger.log("Filled in g-recaptcha-response text-area");
					textarea.sendKeys(gResponse);
				} else {
					Logger.log("Could not find g-recaptcha-response text-area");
				}

				Logger.log("Scrolling");
				driver.switchTo().defaultContent();
				jse.executeScript("window.scrollBy(0,250)", "");
				TimeUnit.SECONDS.sleep(6);
				jse.executeScript("onSubmit()");
				// submit.sendKeys(Keys.ENTER);
				TimeUnit.SECONDS.sleep(6); // added this for leaving the captcha too fast
				waitForLoad(driver);
				TimeUnit.SECONDS.sleep(20); // added this for leaving the captcha too fast
			} else {
				Logger.log("Page failed to load..");
			}

			// check if recovery was successful
			if (driver.findElements(By.id("p-account-recovery-pre-confirmation")).size() != 0) {
				Logger.log("Successfully sent recovery");
				Logger.log("lets check mail");
				doEmailLogin(driver, "sojusopa@yourweb.email");
			} else {
				Logger.log("something went wrong");
			}
		} catch (Exception e) {

		}

	}

	private static boolean doEmailCheck(WebDriver driver) throws InterruptedException {
		driver.get(EMAIL_REFRESH_URL);
		Logger.log("Waiting for Page Load...");
		waitForLoad(driver);

		List<WebElement> elements = driver.findElements(By.className("title-subject"));
		String ourMailLink = null;
		for (WebElement element : elements) {
			String text = element.getText();
			Logger.log(text);
			if (text.contains("Reset your Jagex password")) {
				Logger.log("found mess");
				ourMailLink = element.getAttribute("href");
			}
		}
		OUR_MAIL_LINK = ourMailLink;
		return true;
	}

	private static boolean doEmailLogin(WebDriver driver, String loginEmail) throws InterruptedException {
		driver.get(EMAIL_SET_URL);
		Logger.log("Waiting for Page Load...");
		waitForLoad(driver);

		WebElement inputMail = null, inputDomain = null, submit = null;
		for (int i = 0; i < 3; i++) {
			Logger.log("Page Loaded");
			try {
				inputMail = driver.findElement(By.name("mail"));
				inputDomain = driver.findElement(By.name("domain"));
				submit = driver.findElement(By.id("postbut"));
			} catch (Exception ex) {
				ex.printStackTrace();
				Logger.log("Retrying..");
			}
			if (inputMail == null || inputDomain == null) {
				TimeUnit.MILLISECONDS.sleep(700);
				continue;
			}
			break;
		}

		WebElement initialMailLabel = driver.findElement(By.id("mail"));
		String initialMail = initialMailLabel.getText();

		String firstHalf = loginEmail.substring(0, loginEmail.indexOf('@'));
		String secondHalf = loginEmail.substring(loginEmail.indexOf('@'));
		Logger.log(secondHalf);
		inputMail.sendKeys(firstHalf);
		inputDomain.sendKeys(secondHalf);
		submit.click();
		TimeUnit.SECONDS.sleep(6);
		return true;
	}

	public String getCaptchaResponse() {
		int attempts = 0;
		boolean completed = false;
		String token = null;
		while (token == null) {
			if (attempts < 5) {
				switch (CAPTCHA_SOLVER) {
				case "anticaptcha":
					AntiCaptcha antiCaptcha = new AntiCaptcha();
					try {
						token = antiCaptcha.solveCaptcha(RECOVERY_URL);
						completed = true;
						return token;
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
			}
		}
		return null;

	}

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
		// System.out.println(resource);
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

	static FirefoxProfile copyProfileData(FirefoxProfile profile, PrivateProxy proxy) {
		try {
			Field profileFolderVal = profile.getClass().getDeclaredField("model");
			profileFolderVal.setAccessible(true);
			File profileFolder = (File) profileFolderVal.get(profile);
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

			Path proxySwitchSett = Paths.get(profileFolder.getPath(),
					"browser-extension-data/{0c3ab5c8-57ac-4ad8-9dd1-ee331517884d}/storage.js");
			if (Files.exists(proxySwitchSett)) {
				Charset charset = StandardCharsets.UTF_8;
				String content = new String(Files.readAllBytes(proxySwitchSett), charset);
				content = content.replaceAll("%HOST%", proxy.host).replaceAll("%PORT%", proxy.port)
						.replaceAll("%USERNAME%", proxy.username).replaceAll("%PASSWORD%", proxy.password);
				Files.write(proxySwitchSett, content.getBytes(charset));
			}

			profile.setPreference("extensions.pendingOperations", true);
			profile.setPreference("services.sync.globalScore", 606);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return profile;
	}

	static boolean isNullOrEmpty(String str) {
		if (str == null)
			return true;
		if (str.length() < 3)
			return true;
		return false;
	}

	

	private static boolean ipIsRight(WebDriver driver, String host) {
		driver.get("http://ipv4.plain-text-ip.com/");
		waitForLoad(driver);
		String myIP = driver.findElement(By.tagName("body")).getText();
		if (!myIP.contains(host)) {
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

	public static void createAccountUnlocked(String email) {
		Logger.log("CREATED UNLOCKED MESSAGE");
		NexHelper.messageQueue.push("unlocked_account:" + email + ":" + newPassword);
	}

}

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

public class AccountChangeMail {

	private static final String RUNESCAPE_URL = "https://secure.runescape.com/m=weblogin/loginform.ws?mod=www&ssl=1&expired=0&dest=account_settings";
	private static final String RANDGEN_URL = "https://randomuser.me/api/?nat=gb";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
	private static String CAPTCHA_SOLVER = "anticaptcha";
	// private static String token = null;
	private static int cooldown = 90;

	public static void main(String[] args) {

		new AccountChangeMail();
	}

	public AccountChangeMail() {
		String gResponse = getCaptchaResponse();
		PrivateProxy proxy = new PrivateProxy("", "", "92.32.69.103", "8888");
		String username = "desktdu@gmail.com";
		String password = "ugot00wned2";
		if (gResponse != null) {
			logIntoRunescape(gResponse, proxy, username, password);
		}else {
			Logger.log("Response == null");
		}
	}

	public void logIntoRunescape(String gResponse, PrivateProxy proxy, String username, String password) {
		// setFirefoxDriver();
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

			driver.get(RUNESCAPE_URL);
			Logger.log("Waiting for Page Load...");
			waitForLoad(driver);
			Logger.log("Site loaded.. Lets fill in username details");
			WebElement formUsername = driver.findElement(By.name("username"));
			WebElement formPassword = driver.findElement(By.name("password"));
			WebElement submitButton = driver.findElement(By.id("du-login-submit"));
			WebElement textarea = driver.findElement(By.id("g-recaptcha-response"));

			if (formUsername != null && formPassword != null) {
				formUsername.sendKeys(username);
				formPassword.sendKeys(password);
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
		} catch (Exception e) {

		}

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
						token = antiCaptcha.solveCaptcha(RUNESCAPE_URL);
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

	public boolean createAccount(String username, String email, String password, PrivateProxy proxy, String address) {
		Logger.log("Waiting for captcha code... This might take a while...");
		if (proxy != null && proxy.host.length() > 5) {
			Logger.log("Connecting to Proxy " + proxy.host + ":" + proxy.port);
			if (!setProxy(proxy))
				return false;
			Logger.log("Successfully connected");
		}
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
			if (token == null)
				try {
					Thread.sleep(2000);
				} catch (Exception ex) {
				}
		}
		if (completed) {
			try {
				postForm(token, username, email, password, proxy, address);
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
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

			boolean created = false;
			boolean captchaFailed = false;
			int attempts = 0;
			while (!failed && !created && !captchaFailed) {
				driver.get(RUNESCAPE_URL);
				Logger.log("Waiting for Page Load...");
				waitForLoad(driver);
				TimeUnit.SECONDS.sleep(1);

				WebElement dobDay = null, dobMonth = null, dobYear = null, email = null, password = null,
						textarea = null, submit = null;
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
					} catch (Exception ex) {
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
					if (dobDay == null || dobMonth == null || dobYear == null || email == null || password == null
							|| textarea == null || submit == null) {
						TimeUnit.MILLISECONDS.sleep(2000);
						continue;
					}
					break;
				}

				Random r = new Random();
				String year = (1980 + (int) (r.nextDouble() * 20)) + "";
				String month = (1 + (int) (r.nextDouble() * 10)) + "";
				String day = (1 + (int) (r.nextDouble() * 26)) + "";
				Logger.log("Elements found");
				dobDay.sendKeys(day);
				dobMonth.sendKeys(month);
				dobYear.sendKeys(year);
				email.sendKeys(loginEmail);
				// displayname.sendKeys("williamsosos");
				password.sendKeys(loginPassword);

				Logger.log("Form filled");
				JavascriptExecutor jse = (JavascriptExecutor) driver;

				jse.executeScript("arguments[0].style.display = 'block';", textarea);
				if (gresponse != null && textarea != null) {
					Logger.log("Filled in g-recaptcha-response text-area");
					textarea.sendKeys(gresponse);
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

				Logger.log("Opening Captcha");

				for (int i = 0; i < 10; i++) {
					waitForLoad(driver);
					if (driver.findElements(By.id("p-create-error")).size() != 0) {
						Logger.log("Errooororo. lets send message timeout 10min");
						createIPCooldownMessage(proxy.host, 120);
						TimeUnit.SECONDS.sleep(3);
						break;
					} else if (driver.findElements(By.className("m-character-name-alts__name")).size() != 0) {
						System.out.println("Username In Use - Trying another");
						WebElement newUsername = driver.findElement(By.className("m-character-name-alts__name"));
						newUsername.click();
						waitForLoad(driver);
						// submit.sendKeys(Keys.ENTER);
						TimeUnit.SECONDS.sleep(3);
					} else if (driver.findElements(By.className("google-recaptcha-error")).size() != 0) {
						Logger.log("Google Recaptcha Error");
						captchaFailed = true;
					}

					waitForLoad(driver);
					TimeUnit.SECONDS.sleep(1);

					if (driver.findElements(By.id("p-account-created")).size() != 0) {
						created = true;
						System.out.println("Account Created");
						String parsedProxy = "-proxy " + proxy.host + ":" + proxy.port + ":" + proxy.username + ":"
								+ proxy.password;
						AccountLauncher.launchClient(address);
					}
					if (created)
						break;
				}
				if (!created) {
					created = true;

				}

			}
		} finally {
			try {
				if (driver != null)
					driver.quit();
			} catch (Exception e) {
			}
		}

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

	public static void createIPCooldownMessage(String host, int time) {
		Logger.log("CREATED BAD IP COOLDOWNMESS");
		NexHelper.messageQueue.push("ip_cooldown:" + host + ":" + time);
	}

}

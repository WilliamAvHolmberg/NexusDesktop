import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.medusa.Utils.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountCreatorTest {
	private static final String RUNESCAPE_URL = "https://secure.runescape.com/m=account-creation/create_account";
	private static final String RANDGEN_URL = "https://randomuser.me/api/?nat=gb";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
	private static String CAPTCHA_SOLVER = "anticaptcha";
	private static String token = null;
	

	public static void createZip(PrivateProxy proxy)
			throws IOException {
		FileOutputStream fout = new FileOutputStream("extension.zip");
		ZipOutputStream zout = new ZipOutputStream(fout);
		ClassLoader classLoader = AccountCreatorTest.class.getClassLoader();
		URL resource = classLoader.getResource("background.js");
		System.out.println(resource);
		File background = new File("background.js");
		if (!background.exists()) {
			try {
				background.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			background.setExecutable(true);
			try {
				org.apache.commons.io.FileUtils.copyURLToFile(resource, background);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		URL resource2 = classLoader.getResource("manifest.json");
		System.out.println(resource);
		File driver = new File("manifest.json");
		if (!driver.exists()) {
			try {
				driver.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			driver.setExecutable(true);
			try {
				org.apache.commons.io.FileUtils.copyURLToFile(resource2, driver);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//background js that will be dynamic for us
		Charset charset = StandardCharsets.UTF_8;
		//read js file and replace proxy details
		String content = new String(Files.readAllBytes(background.toPath()), charset);
		content = content.replaceAll("proxy_type", "http");
		content = content.replaceAll("proxy_host", proxy.host);
		content = content.replaceAll("proxy_port", "10000");
		content = content.replaceAll("proxy_username", proxy.username);
		content = content.replaceAll("proxy_password", proxy.password);

		//add modified background.js to zip
		ZipEntry ze = new ZipEntry(background.getPath());
		zout.putNextEntry(ze);
		zout.write(content.getBytes(), 0, content.getBytes().length);
		zout.closeEntry();
		
		
		//add manifest to zip
		File manifest = new File("manifest.json");
		ZipEntry zeManifest = new ZipEntry(manifest.getPath());
		zout.putNextEntry(zeManifest);
		byte[] bytes = Files.readAllBytes(manifest.toPath());
		zout.write(bytes, 0, bytes.length);
		zout.closeEntry();
		zout.close();
	}


	
	public void createAccount(String username, String email, String password, PrivateProxy proxy, String address) {
		Logger.log("Waiting for captcha code... This might take a while...");
		int attempts = 0;
		try {
			createZip(proxy);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (token == null) {
			if (attempts < 5) {
				switch (CAPTCHA_SOLVER) {
				case "anticaptcha":
					AntiCaptcha antiCaptcha = new AntiCaptcha();
					try {
						token = antiCaptcha.solveCaptcha(RUNESCAPE_URL);
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
				System.gc();
				System.exit(0);
			}
		}

		try {
				postForm(token, username, email, password, proxy, address);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private static void setChromeDriver() {
		ClassLoader classLoader = AccountCreatorTest.class.getClassLoader();
		URL resource = classLoader.getResource("drivers/" + getDriverNameChrome());
		System.out.println(resource);
		File f = new File("Driver");
		if (!f.exists()) {
			f.mkdirs();
		}
		File driver = new File("Driver" + File.separator + getDriverNameChrome());
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
		System.setProperty("webdriver.chrome.driver", driver.getAbsolutePath());
		// driver = new ChromeDriver();
		System.setProperty("webdriver.chrome.driver", driver.getAbsolutePath());
	}

	private static String getDriverNameChrome() {
		switch (AccountLauncher.getOperatingSystemType()) {
		case Linux:
			return "chromedriver_linux";
		case MacOS:
			return "chromedriver";
		case Windows:
			return "geckodriver.exe";
		default:
			break;

		}
		return null;
	}


	private static void postForm(String gresponse, String username, String loginEmail, String loginPassword,
		PrivateProxy proxy, String address) throws Exception {
		setChromeDriver();
		
		ChromeOptions options = new ChromeOptions();
		options.addExtensions(new File("extension.zip"));
		ChromeDriver driver = new ChromeDriver(options);
		driver.get("https://www.ipinfo.io");

		driver.manage().window().maximize();

		boolean created = false;
		boolean captchaFailed = false;
		
		while (!created && !captchaFailed) {
			driver.get(RUNESCAPE_URL);
			waitForLoad(driver);

			WebElement dobDay = driver.findElement(By.name("day"));
			WebElement dobMonth = driver.findElement(By.name("month"));
			WebElement dobYear = driver.findElement(By.name("year"));
			WebElement email = driver.findElement(By.name("email1"));
			WebElement password = driver.findElement(By.name("password1"));
			WebElement textarea = driver.findElement(By.id("g-recaptcha-response"));
			WebElement submit = driver.findElement(By.id("create-submit"));

			dobDay.sendKeys("01");
			dobMonth.sendKeys("01");
			dobYear.sendKeys("1990");
			email.sendKeys(loginEmail);
			password.sendKeys(loginPassword);

			JavascriptExecutor jse = (JavascriptExecutor) driver;
			jse.executeScript("arguments[0].style.display = 'block';", textarea);

			textarea.sendKeys(gresponse);

			driver.switchTo().defaultContent();
			jse.executeScript("window.scrollBy(0,250)", "");
			TimeUnit.SECONDS.sleep(6);
			jse.executeScript("onSubmit()");
			submit.sendKeys(Keys.ENTER);
			TimeUnit.SECONDS.sleep(6);
			waitForLoad(driver);

			if (driver.findElements(By.className("m-character-name-alts__name")).size() != 0) {
				System.out.println("Username In Use - Trying another");
				WebElement newUsername = driver.findElement(By.className("m-character-name-alts__name"));
				newUsername.click();
				waitForLoad(driver);
				// submit.sendKeys(Keys.ENTER);
				TimeUnit.SECONDS.sleep(3);
			} else if (driver.findElements(By.className("google-recaptcha-error")).size() != 0) {
				captchaFailed = true;
			}

			if (driver.findElements(By.id("p-account-created")).size() != 0) {
				created = true;
				System.out.println("Account Created");
				AccountLauncher.launchClient(address);
			} else {
				created = true;
				System.out.println("We failed. lets not retry -");

			}
			token = null;
		}

		driver.quit();
	}

}

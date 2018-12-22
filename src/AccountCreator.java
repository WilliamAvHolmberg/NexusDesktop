
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AccountCreator {

    private static final String RUNESCAPE_URL = "https://secure.runescape.com/m=account-creation/create_account";
    private static final String RANDGEN_URL = "https://randomuser.me/api/?nat=gb";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
    private static String CAPTCHA_SOLVER = "anticaptcha";
    private static String token = null;


    void setProxy(PrivateProxy currentProxy) {
		System.getProperties().put("proxySet", "true");
		System.getProperties().put("socksProxyHost", currentProxy.host);
		System.getProperties().put("socksProxyPort", currentProxy.port);
		Authenticator.setDefault(new ProxyAuth(currentProxy.username, currentProxy.password));
		URL whatismyip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			String ip = in.readLine(); // you get the IP as a String
			System.out.println(ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
    public void createAccount(String username, String email, String password, PrivateProxy proxy, String address) {
    	Logger.log("Waiting for captcha code... This might take a while...");
		if (proxy != null && proxy.host.length() > 5) {
			Logger.log("Hello");
			setProxy(proxy);
			Logger.log("hello again");
		}
            int attempts = 0;
            
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
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }


    private static void setDriver() {
    	ClassLoader classLoader = Test.class.getClassLoader();
        URL resource = classLoader.getResource("drivers/" + getDriverName());
        System.out.println(resource);
        File f = new File("Driver");
        if (!f.exists()) {
            f.mkdirs();
        }
        File driver = new File("Driver" + File.separator + getDriverName());
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
        //driver = new ChromeDriver();
        System.setProperty("webdriver.gecko.driver", driver.getAbsolutePath());
    }

   
    private static void postForm(String gresponse,String username, String loginEmail, String loginPassword, PrivateProxy proxy, String address) throws Exception {
    	 //ChromeOptions options = new ChromeOptions();
    	    // setting headless mode to true.. so there isn't any ui
    	 //   options.setHeadless(true);

    	    // Create a new instance of the Chrome driver
    	  //  WebDriver driver = new ChromeDriver(options);       
 
         /*  System.out.println("lets get path");
           FirefoxOptions options = new FirefoxOptions();
           FirefoxProfile profile = new FirefoxProfile();
           profile.setPreference("network.proxy.type", 1);
           profile.setPreference("network.proxy.socks", proxy.host);
           profile.setPreference("network.proxy.socks_port", proxy.port);
           
           options.setProfile(profile);*/
    	setDriver();
        FirefoxOptions options = new FirefoxOptions();

    	FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("network.proxy.type", 1);
        profile.setPreference("network.proxy.socks", proxy.host);
        profile.setPreference("network.proxy.socks_port", Integer.parseInt(proxy.port));
        options.setProfile(profile);
        FirefoxDriver driver = new FirefoxDriver(options);
        driver.get("https://www.ipinfo.io");
		
        driver.manage().window().maximize();

        boolean created = false;
        boolean captchaFailed = false;
        int attempts = 0;
        while(!created && !captchaFailed) {
            driver.get(RUNESCAPE_URL);
            waitForLoad(driver);

            WebElement dobDay = driver.findElement(By.name("day"));
            WebElement dobMonth = driver.findElement(By.name("month"));
            WebElement dobYear = driver.findElement(By.name("year"));
            WebElement email = driver.findElement(By.name("email1"));
           // WebElement displayname = driver.findElement(By.name("displayname"));
            WebElement password = driver.findElement(By.name("password1"));
            WebElement textarea = driver.findElement(By.id("g-recaptcha-response"));
            WebElement submit = driver.findElement(By.id("create-submit"));
           
            dobDay.sendKeys("01");
            dobMonth.sendKeys("01");
            dobYear.sendKeys("1990");
            email.sendKeys(loginEmail);
           // displayname.sendKeys("williamsosos");
            password.sendKeys(loginPassword);

            JavascriptExecutor jse = (JavascriptExecutor)driver;
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
                //submit.sendKeys(Keys.ENTER);
                TimeUnit.SECONDS.sleep(3);
            } else if (driver.findElements(By.className("google-recaptcha-error")).size() != 0) {
                captchaFailed = true;
            }

            
            if (driver.findElements(By.id("p-account-created")).size() != 0) {
                created = true;
                System.out.println("Account Created");
                String parsedProxy = "-proxy " + proxy.host + ":" + proxy.port+ ":" + proxy.username + ":" + proxy.password;
				AccountLauncher.launchClient(address);
            }else {
            	created = true;
                System.out.println("We failed. lets not retry -");

            }
            token = null;
        }

        driver.quit();
    }

    

  

    private static String getDriverName() {
		switch(AccountLauncher.getOperatingSystemType()) {
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

	private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            char[] chars = new char[1024];

            int read;
            while((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            String var7 = buffer.toString();
            return var7;
        } finally {
            if (reader != null) {
                reader.close();
            }

        }
    }
}
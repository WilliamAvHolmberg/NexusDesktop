import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.openqa.selenium.chrome.ChromeDriver;

public class Test {
	
	public static void main(String[] args) throws IOException {
		ClassLoader classLoader = Test.class.getClassLoader();
        URL resource = classLoader.getResource("drivers/geckodriver");
        System.out.println(resource);
        File f = new File("Driver");
        if (!f.exists()) {
            f.mkdirs();
        }
        File driver = new File("Driver" + File.separator + "geckodriver");
        if (!driver.exists()) {
            driver.createNewFile();
            driver.setExecutable(true);
            org.apache.commons.io.FileUtils.copyURLToFile(resource, driver);
        }
        System.setProperty("webdriver.gecko.driver", driver.getAbsolutePath());
        //driver = new ChromeDriver();
	}

}

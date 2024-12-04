package data.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * The ChromeSetup class process to set up ChromeDriver, initialize WebDriver and some property,
 * finally return the driver back for further use.
 */

public class ChromeSetup {
      private static final String CHROMEDRIVER_PATH
            = "C:\\Users\\admin\\OneDrive\\Máy tính\\chromedriver.exe";

      public static WebDriver set() {
            System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);
            WebDriver driver = new ChromeDriver();
            driver.manage().window().maximize();

            return driver;
      }
}


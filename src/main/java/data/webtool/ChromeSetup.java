package data.webtool;

import data.package_config.Constant;
import data.package_config.FilePath;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;


/**
 * The ChromeSetup class process to set up ChromeDriver, initialize WebDriver and some property,
 * finally return the driver back for further use.
 */
public class ChromeSetup {
      public static WebDriver set() {
            System.setProperty("webdriver.chrome.driver", FilePath.CHROMEDRIVER_PATH);
            WebDriver driver = new ChromeDriver();
            driver.manage().window().maximize();

            return driver;
      }
}

      ;
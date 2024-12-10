package data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import data.constant.Constant;
import data.util.Sleeper;
import org.openqa.selenium.WebDriver;

/**
 * The Crawler interface defines a basic method of crawling data using -driver- to navigate
 * to the target web, crawl data here and push them into -target_jsonObject- using -gson-
 */

public abstract class Crawler {
      public WebDriver driver;
      public Gson gson;
      public JsonObject target_jsonObject;

      public Crawler (WebDriver driver, Gson gson) {
            this.driver = driver;
            this.gson = gson;
      }

      public Crawler (WebDriver driver, Gson gson, JsonObject target_jsonObject) {
            this.driver = driver;
            this.gson = gson;
            this.target_jsonObject = target_jsonObject;
      }

      public void navigate () {}
      public abstract boolean crawl ();
      public void navigateBack () {
            driver.navigate().back();
            Sleeper.sleep(Constant.SMALL_WAIT_TIME);
      }
}

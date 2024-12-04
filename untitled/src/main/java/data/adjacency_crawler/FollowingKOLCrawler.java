package data.adjacency_crawler;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import constant.TimeConstant;
import data.util.PageWait;
import data.util.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import data.Crawler;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FollowingKOLCrawler extends Crawler {
      private static final int MAX_PROFILE_COUNT = 50;
      private static final int SCROLL_LENGTH = 2500;

      private JsonObject kol_map_jsonObject;


      /// ____Constructor____ ///
      public FollowingKOLCrawler (WebDriver driver, Gson gson,
                                  JsonObject target_jsonObject, JsonObject kol_map_jsonObject) {
            super(driver, gson, target_jsonObject);
            this.kol_map_jsonObject = kol_map_jsonObject;
      }


      /// ____Method____ ///
      @Override
      public void navigate() {
            // click the div that shows the following count
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(TimeConstant.HUGE_WAIT_TIME));

            String handle_name = target_jsonObject.get("handle").getAsString().substring(1);
            WebElement following_webElement = wait.until(
                  ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(STR."a[href='/\{handle_name}/following']")
                  )
            );

            following_webElement.click();
            PageWait.wait(driver);
      }

      @Override
      public void crawl () {
            System.out.println("Inspecting following profiles");

            JavascriptExecutor js_executor = (JavascriptExecutor) driver;
            Set<String> handles = new HashSet<>();

            while (true) {
                  List<WebElement> cur_handle_webElement_list = driver.findElements
                        (By.cssSelector("div[data-testid='primaryColumn'] " +
                              "div.css-175oi2r.r-1awozwy.r-18u37iz.r-1wbh5a2"));

                  int old_size = handles.size();
                  for (WebElement handle_webElement: cur_handle_webElement_list) {
                        String handle = handle_webElement.getAttribute("textContent");
                        handles.add(handle);
                        System.out.println(handle);
                  }
                  int new_size = handles.size();

                  if (new_size == old_size) break;

                  js_executor.executeScript(STR."window.scrollBy(0, \{SCROLL_LENGTH});");
                  Sleeper.sleep(TimeConstant.SMALL_WAIT_TIME);
            }

            for (String handle: handles) {
                  if (kol_map_jsonObject.has(handle)) {
                        target_jsonObject.get("following_kol_handle_list").getAsJsonArray().add(handle);
                  }
            }
      }

      public void navigateBack () {
            driver.navigate().back();
            PageWait.wait(driver);
      }
}

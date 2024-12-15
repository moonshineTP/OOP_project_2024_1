package data.kol_adjacency_crawler;

import com.google.gson.Gson;

import com.google.gson.JsonObject;
import data.constant.Constant;
import data.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import data.Crawler;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to crawl the following KOLs of the inspected KOL and add the info to the
 * following_kol_handle_list;
 */
public class FollowingCrawler extends Crawler {
      private static final int SCROLL_LENGTH = 2500;

      private JsonObject kol_map_jsonObject;


      /// ____Constructor____ ///
      public FollowingCrawler(WebDriver driver, Gson gson,
                              JsonObject target_jsonObject, JsonObject kol_map_jsonObject) {
            super(driver, gson, target_jsonObject);
            this.kol_map_jsonObject = kol_map_jsonObject;
      }


      /// ____Method____ ///
      @Override
      public void navigate() {
            System.out.println("/// Inspecting following KOL");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(Constant.BIG_WAIT_TIME));

            String handle_name = target_jsonObject.get("handle").getAsString().substring(1);
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                  By.cssSelector("a[href='/" + handle_name + "/following']"))).click();

            Sleeper.sleep(Constant.BIG_WAIT_TIME);
      }

      @Override
      public boolean crawl () {
            /// Crawl
            Set<String> handles = new HashSet<>();
            HandleCrawler handle_crawler = new HandleCrawler(driver);

            boolean crawl_state;
            do {
                  crawl_state = handle_crawler.crawl(handles);
            } while (crawl_state);

            /// Push data
            int kol_count = 0;
            for (String handle: handles) {
                  if (kol_map_jsonObject.has(handle)) {
                        kol_count++;
                        System.out.println("KOL: " + handle);
                        target_jsonObject.get("following_kol_handle_list").getAsJsonArray().add(handle);
                  }
            }

            /// Finish
            System.out.println(kol_count + " KOL(s) found\n");
            Sleeper.sleep(Constant.SMALL_WAIT_TIME);
            return true;
      }
}

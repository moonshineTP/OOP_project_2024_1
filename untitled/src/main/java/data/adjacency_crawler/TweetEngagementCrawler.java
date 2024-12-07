package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import data.Crawler;
import data.constant.Constant;
import data.util.Sleeper;
import graph_element.User;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;


/**
 * This class is used to crawl the user engagements of a tweet
 * and push the data into user_data_jsonObject.
 */

public class TweetEngagementCrawler extends Crawler {
      /// ____Field____ ///
      private JsonObject user_data_jsonObject;
      private WebDriverWait wait;
      private JavascriptExecutor js_executor;

      /// ____Constructor____ ///
      public TweetEngagementCrawler (WebDriver driver, Gson gson, JsonObject target_jsonObject,
                                     JsonObject user_data_jsonObject) {
            super(driver, gson, target_jsonObject);
            this.user_data_jsonObject = user_data_jsonObject;
            wait = new WebDriverWait(driver, Duration.ofMillis(Constant.HUGE_WAIT_TIME));
            js_executor = (JavascriptExecutor) driver;
      }

      /// ____Method____ ///
      public void navigateToQuoteList () {
            // get the statistic board below the tweet
            WebElement statistic_board = wait.until(ExpectedConditions.visibilityOfElementLocated(
                  By.cssSelector(".css-175oi2r.r-1kbdv8c.r-18u37iz.r-1oszu61.r-3qxfft.r-n7gxbd" +
                        ".r-2sztyj.r-1efd50x.r-5kkj8d.r-h3s6tt.r-1wtj0ep.r-1igl3o0.r-rull8r.r-qklmqi")
            ));

            // get the distance from the board to the top
            long distance_to_top;
            Object distance_to_top_object = js_executor.executeScript(
                  "return arguments[0].getBoundingClientRect().top;", statistic_board);
            if (distance_to_top_object instanceof Double) {
                  distance_to_top = ((Double) distance_to_top_object).longValue();
            } else distance_to_top = (long) distance_to_top_object;

            // scroll exactly to the board
            js_executor.executeScript(STR."window.scrollBy(0, \{distance_to_top - 46});");

            // click the quote button
            js_executor.executeScript("document.elementFromPoint"
                  + "(arguments[0], arguments[1]).click();", 540, 90);
            Sleeper.sleep(Constant.SMALL_WAIT_TIME);

            // click to view quote
            js_executor.executeScript("document.elementFromPoint"
                  + "(arguments[0], arguments[1]).click();", 540, 150);
            Sleeper.sleep(Constant.BIG_WAIT_TIME);
      }

      public void navigateToRepostListFromQuoteList() {
            js_executor.executeScript("document.elementFromPoint"
                  + "(arguments[0], arguments[1]).click();", 800, 100);
            Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -2000);");
            Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
      }

      public void navigateBackToCommentList() {
            navigateBack();
            navigateBack();
            Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
      }

      @Override
      public boolean crawl() {
            /// Initialize crawler
            HandleCrawler handle_crawler = new HandleCrawler(driver);

            /// Crawl
            Set<String> quote_handle_set = new HashSet<>();
            Set<String> repost_handle_set = new HashSet<>();
            Set<String> comment_handle_set = new HashSet<>();
            boolean crawl_state;

            // navigate to the quote list
            navigateToQuoteList();
            do { // crawl
                  crawl_state = handle_crawler.crawl(quote_handle_set);
            } while (crawl_state);
            // remove the author's handle
            quote_handle_set.remove(target_jsonObject.get("handle").getAsString());
            // announce the result
            System.out.println(STR."- \{quote_handle_set.size()} quotes crawled");

            // navigate to the repost list
            navigateToRepostListFromQuoteList();
            do { // crawl
                  crawl_state = handle_crawler.crawl(repost_handle_set);
            } while (crawl_state);
            // remove the author's handle
            repost_handle_set.remove(target_jsonObject.get("handle").getAsString());
            // announce the result
            System.out.println(STR."- \{repost_handle_set.size()} reposts crawled");

            // navigate back to the tweet
            navigateBackToCommentList();
            do { // crawl
                  crawl_state = handle_crawler.crawl(comment_handle_set);
            } while (crawl_state);
            // remove the author's handle
            comment_handle_set.remove(target_jsonObject.get("handle").getAsString());
            // announce the result
            System.out.println(STR."- \{comment_handle_set.size()} comments crawled");

            /// Push the data to the json objects
            pushData(quote_handle_set, TweetEngagementType.QUOTE);
            pushData(repost_handle_set, TweetEngagementType.REPOST);
            pushData(comment_handle_set, TweetEngagementType.COMMENT);

            /// Return
            return true;
      }

      private void pushData(Set<String> handles, TweetEngagementType type) {
            // get the tweet's id
            String url = driver.getCurrentUrl();                        // get the tweet's url
            String id = url.substring(url.indexOf("/status/") + 8);     // extract the id

            // get the KOL and Non-KOL JsonObject
            JsonObject kol_data_jsonObject = user_data_jsonObject.get("KOL").getAsJsonObject();
            JsonObject non_kol_data_jsonObject = user_data_jsonObject.get("Non-KOL").getAsJsonObject();

            // push data
            for (String handle: handles) {

                  JsonObject user_jsonObject;

                  if (kol_data_jsonObject.has(handle)) {    // retrieve the kol in the kol_data (if exists)
                        user_jsonObject = kol_data_jsonObject.get("handle").getAsJsonObject();
                  }
                  else {      // if not, initialize a new user and add it to the Non-KOL
                        User user = new User(handle);
                        user_jsonObject = gson.toJsonTree(user).getAsJsonObject();
                        non_kol_data_jsonObject.add(handle, user_jsonObject);
                  }

                  // push the id into the corresponding list
                  String list_type = switch (type) {  case QUOTE -> "quote_tweet_id_list";
                                                      case REPOST -> "repost_tweet_id_list";
                                                      case COMMENT -> "comment_tweet_id_list";  };
                  user_jsonObject.get(list_type).getAsJsonArray().add(id);
            }
      }
}







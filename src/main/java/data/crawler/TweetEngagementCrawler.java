package data.crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import data.package_config.Constant;
import data.util.Sleeper;
import twitter_model.User;
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
      private String base_url;

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
            driver.navigate().to(base_url + "/quotes");
            Sleeper.sleep(Constant.BIG_WAIT_TIME);
      }

      public void navigateToRepostList() {
            driver.navigate().to(base_url + "/retweets");
            Sleeper.sleep(Constant.BIG_WAIT_TIME);
      }

      public void navigateToCommentList() {
            driver.navigate().to(base_url);
            Sleeper.sleep(Constant.BIG_WAIT_TIME);
      }

      @Override
      public boolean crawl() {
            /// Initialize crawler
            HandleCrawler handle_crawler = new HandleCrawler(driver);

            /// Initialize handle containers
            Set<String> quote_handle_set = new HashSet<>();
            Set<String> repost_handle_set = new HashSet<>();
            Set<String> comment_handle_set = new HashSet<>();
            boolean crawl_state;

            /// Get the base url;
            base_url = driver.getCurrentUrl();

            /// Navigate to the quote list
            navigateToQuoteList();
            do { // crawl
                  crawl_state = handle_crawler.crawl(quote_handle_set);
            } while (crawl_state);
            // remove the author's handle
            quote_handle_set.remove(target_jsonObject.get("handle").getAsString());
            // announce the result
            System.out.println("- " + quote_handle_set.size() + " quotes crawled");


            /// Navigate to the repost list
            navigateToRepostList();
            do { // crawl
                  crawl_state = handle_crawler.crawl(repost_handle_set);
            } while (crawl_state);
            // remove the author's handle
            repost_handle_set.remove(target_jsonObject.get("handle").getAsString());
            // announce the result
            System.out.println("- " + repost_handle_set.size() + " reposts crawled");


            /// Navigate back to the tweet
            navigateToCommentList();
            int fail_count = 0, max_fail = 3;
            do { // crawl
                  crawl_state = handle_crawler.crawl(comment_handle_set);
                  if (!crawl_state) fail_count++;
                  else fail_count = 0;
            } while (fail_count < max_fail);
            // remove the author's handle
            comment_handle_set.remove(target_jsonObject.get("handle").getAsString());
            // announce the result
            System.out.println("- " + comment_handle_set.size() + " comments crawled");


            /// Push the data to the json objects
            pushData(quote_handle_set, TweetEngagementType.QUOTE);
            pushData(repost_handle_set, TweetEngagementType.REPOST);
            pushData(comment_handle_set, TweetEngagementType.COMMENT);


            /// Finish
            System.out.println("- TweetUserRole engagament crawled \n");
            Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
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
                        user_jsonObject = kol_data_jsonObject.get(handle).getAsJsonObject();
                  } else if (non_kol_data_jsonObject.has(handle)) {
                        user_jsonObject = non_kol_data_jsonObject.get(handle).getAsJsonObject();
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

      enum TweetEngagementType {
            QUOTE,
            REPOST,
            COMMENT,
      }
}







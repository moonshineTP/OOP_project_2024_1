package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.constant.Constant;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import data.Crawler;
import data.Sleeper;

import java.time.Duration;
import java.util.List;

/**
 * This class is used to crawl the most recent and useful tweets of a user
 */

public class KOLTweetsCrawler extends Crawler {
      /// ____Constant____ ///
      private static final int FAIL_COUNT_LIMIT = 10;

      /// ____Field____ ///
      private TweetCrawler tweet_crawler;
      private WebDriverWait wait;
      private JavascriptExecutor js_executor;

      /// ____Constructor____ ///
      public KOLTweetsCrawler(WebDriver driver, Gson gson, JsonObject target_jsonObject,
                              JsonObject user_data_jsonObject, JsonObject tweet_data_jsonObject) {
            super(driver, gson, target_jsonObject);

            tweet_crawler = new TweetCrawler(driver, gson, target_jsonObject,
                  user_data_jsonObject, tweet_data_jsonObject);
            wait = new WebDriverWait(driver, Duration.ofMillis(Constant.HUGE_WAIT_TIME));
            js_executor = (JavascriptExecutor) driver;
      }


      /// ____Method____ ///
      @Override
      public boolean crawl() {
            System.out.println("/// Inspecting tweets ///");

            /// ____Adjust the position of the first div on the screen____ ///
            // get the first div;
            WebElement first_tweet = wait.until(
                  ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div[data-testid='primaryColumn'] div[data-testid='cellInnerDiv']")));

            // get the distance of the first div from the top of the viewpoint
            long distance_to_top;
            Object distance_to_top_object = js_executor.executeScript(
                  "return arguments[0].getBoundingClientRect().top;", first_tweet);
            if (distance_to_top_object instanceof Double) {
                  distance_to_top = ((Double) distance_to_top_object).longValue();
            } else distance_to_top = (long) distance_to_top_object;

            // scroll it to place it right under the header
            js_executor.executeScript(STR."window.scrollBy(0, \{distance_to_top - 53});");


            /// ____Main loop____ ///
            /*    The loop first finds the divs under the mouse position, filter that to get the
                  div that possibly represents a tweet.
                  After a small check to confirm that it is indeed a tweet, we increment the
                  tweet count and start crawling the tweet.
                  After crawling, we scroll down exactly to the next div
                  (note that we need to reselect and click the div).

                  The loop stops when the tweet count exceed the tweet limit or the fail count
                  exceeds the limit. */
            String script  = "return document.elementsFromPoint(arguments[0], arguments[1])"
                  + ".filter(el => el.tagName.toLowerCase() === 'div' "
                  + "&& el.matches(arguments[2]));";
            int fail_count = 0;
            for (int tweet_count = 0; tweet_count < Constant.TWEET_COUNT_LIMIT;) {
                  /// Getting the desired div
                  // get the divs under the position and filter
                  Object possible_divs = js_executor.executeScript(script, 360, 80,
                        "div[data-testid='cellInnerDiv']");

                  // get the inspected div
                  WebElement inspected_div = ((List<WebElement>) possible_divs).getFirst();


                  /// Checking
                  // check the tweet height
                  int tweet_height = Integer.parseInt(inspected_div.getAttribute("offsetHeight"));
                  if (tweet_height < 90) {
                        js_executor.executeScript(STR."window.scrollBy(0, \{tweet_height});");
                        Sleeper.sleep(Constant.SMALL_WAIT_TIME);
                        continue;
                  }

                  // check if it contains any article with data-testid='tweet'
                  List<WebElement> tweets = inspected_div.findElements(
                        By.cssSelector("article[data-testid='tweet']"));
                  if (tweets.isEmpty()) {
                        js_executor.executeScript(STR."window.scrollBy(0, \{tweet_height});");
                        Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
                        continue;
                  }

                  System.out.println("/// A tweet is spotted");


                  /// Crawl the tweet
                  tweet_crawler.navigate();
                  boolean crawl_state = tweet_crawler.crawl();
                  tweet_crawler.navigateBack();

                  /// Update and check for break
                  if (crawl_state) {
                        tweet_count++;
                        fail_count = 0;
                  } else {
                        fail_count++;
                        if (fail_count == FAIL_COUNT_LIMIT) break;
                  }

                  /// Prepare for the next div
                  js_executor.executeScript(STR."window.scrollBy(0, \{tweet_height});");
                  Sleeper.sleep(Constant.SMALL_WAIT_TIME);
            }


            /// ____Announce the crawl is successful____ ///
            System.out.println("/// Tweets extracted successfully");
            return true;
      }
}

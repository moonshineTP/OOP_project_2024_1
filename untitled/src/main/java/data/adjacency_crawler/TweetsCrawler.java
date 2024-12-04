package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import constant.TimeConstant;
import data.Crawler;

import data.util.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TweetsCrawler extends Crawler {
      /// ____Constant____ ///
      private static final int HEADER_HEIGHT = 53;
      private static final int TWEET_COUNT_LIMIT = 20;
      // the position to inspect the tweet div, change if necessary
      private static final int CLICK_POINT_X = 800;
      private static final int CLICK_POINT_Y = 80;

      /// ____Field____ ///
      private JsonObject user_jsonObject;
      private JsonObject tweet_jsonObject;
      // helper crawler
      private OneTweetCrawler one_tweet_crawler;
      // driver stuff :3
      private WebDriverWait wait;
      private JavascriptExecutor js_executor;

      /// ____Constructor____ ///
      public TweetsCrawler (WebDriver driver, Gson gson, JsonObject target_jsonObject,
                            JsonObject user_jsonObject, JsonObject tweet_jsonObject) {
            super(driver, gson, target_jsonObject);
            this.user_jsonObject = user_jsonObject;
            this.tweet_jsonObject = tweet_jsonObject;
            wait = new WebDriverWait(driver, Duration.ofMillis(TimeConstant.HUGE_WAIT_TIME));
            js_executor = (JavascriptExecutor) driver;
      }


      /// ____Method____ ///
      @Override
      public void navigate() {
            driver.navigate().to(target_jsonObject.get("url").getAsString());
      }

      @Override
      public void crawl() {
            System.out.println("/// Inspecting tweets ///");
            /// Initialize crawler
            one_tweet_crawler = new OneTweetCrawler (driver, gson, user_jsonObject, tweet_jsonObject);

            /// Adjust the position of the first div on the screen
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
            js_executor.executeScript(STR."window.scrollBy(0, \{distance_to_top - HEADER_HEIGHT});");

            /// Initialize stuff
            one_tweet_crawler = new OneTweetCrawler(driver, gson, user_jsonObject, tweet_jsonObject);
            String script  = "return document.elementsFromPoint(arguments[0], arguments[1])"
                  + ".filter(el => el.tagName.toLowerCase() === 'div' "
                  + "&& el.matches(arguments[2]));";

            /// Main loop
            for (int tweet_count = 0; tweet_count < TWEET_COUNT_LIMIT;) {
                  Object possible_divs = js_executor.executeScript(script, CLICK_POINT_X,
                        CLICK_POINT_Y, "div[data-testid='cellInnerDiv']");

                  WebElement inspected_divs = ((List<WebElement>) possible_divs).getFirst();

                  if (  !(inspected_divs.findElements(
                              By.cssSelector(":scope > .css-175oi2r.r-1igl3o0.r-qklmqi.r-1adg3ll.r-1ny4l3l")))
                                    .isEmpty())
                  {
                        tweet_count++;
                        System.out.println("This is a tweet");
                        one_tweet_crawler.setTweet(inspected_divs);

                        Sleeper.sleep(TimeConstant.BIG_WAIT_TIME);

                        one_tweet_crawler.navigate();
                        one_tweet_crawler.crawl();
                        one_tweet_crawler.navigateBack();
                  }

                  /// Scroll down for the next tweet
                  int tweet_height = Integer.parseInt(inspected_divs.getAttribute("clientHeight"));
                  js_executor.executeScript(STR."window.scrollBy(0, \{tweet_height});");

                  /// Sleep a lil bit
                  Sleeper.sleep(TimeConstant.SMALL_WAIT_TIME);
            }
      }
}

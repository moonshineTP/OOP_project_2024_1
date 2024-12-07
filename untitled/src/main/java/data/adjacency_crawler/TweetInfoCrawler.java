package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import data.Crawler;
import data.constant.Constant;
import data.util.ConvertToMinutesSinceStart;
import data.util.ConvertTwitterCount;
import data.util.Sleeper;
import graph_element.Tweet;

import java.time.Duration;
import java.util.List;


/**
 * This class is used to check if the tweet satisfies the demand, immediately return false if not.
 * Otherwise, it initializes a Tweet, adds the necessary info
 * and push that into the tweet_data json object.
 */

public class TweetInfoCrawler extends Crawler {
      /// ____Field____ ///
      JsonObject tweet_data_jsonObject;
      WebDriverWait wait;


      /// ____Constructor____ ///
      public TweetInfoCrawler (WebDriver driver, Gson gson, JsonObject target_jsonObject,
                               JsonObject tweet_data_jsonObject) {
            super(driver, gson, target_jsonObject);
            this.tweet_data_jsonObject = tweet_data_jsonObject;
            wait = new WebDriverWait(driver, Duration.ofMillis(Constant.HUGE_WAIT_TIME));
      }


      /// ____Method____ ///
      @Override
      public boolean crawl() {
            /// ____Checking____ ///
            // Check if it is too old
            String timestamp = wait.until(ExpectedConditions.visibilityOfElementLocated(
                  By.cssSelector(".css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0.r-poiln3.r-xoduu5"
                        + ".r-1q142lx.r-1w6e6rj.r-9aw3ui.r-3s2u2q.r-1loqt21")
            )).getAttribute("innerText");
            if (ConvertToMinutesSinceStart.convert(timestamp) < 0) {
                  System.out.println("- The post is too old. Tweet unqualified");
                  return false;
            }

            // Check if it is a repost
            String author_username_and_handle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                  By.cssSelector("div[data-testid='cellInnerDiv'] .css-175oi2r.r-1d09ksm.r-18u37iz.r-1wbh5a2"))
            ).getAttribute("innerText");
            String author = author_username_and_handle.substring(author_username_and_handle.indexOf('\n') + 1);
            String user_handle = target_jsonObject.get("handle").getAsString();
            if (!author.equals(user_handle)) {
                  System.out.println("- Not an original post. Tweet unqualified");
                  return false;
            }

            System.out.println("- Tweet qualified");


            /// ____Tweet qualified____ ///
            // find the tweet's url and id
            String url = driver.getCurrentUrl();      // get the tweet's url
            String id = url.substring(url.indexOf("/status/") + 8);    // extract the id

            // initialize tweet and set its identifying info
            Tweet tweet;
            tweet = new Tweet(id);
            tweet.setInfo(url, author, timestamp);


            /// ____Add the tweet's statistic attribute____ ///
            // ____Get the view count____ //
            String view_count_string = wait.until(ExpectedConditions.visibilityOfElementLocated(
                  By.cssSelector(".css-1jxf684.r-bcqeeo.r-1ttztb7.r-qvutc0"
                        + ".r-poiln3.r-1b43r93.r-1cwl3u0.r-b88u0q")
            )).getAttribute("innerText");
            int view_count = ConvertTwitterCount.convert(view_count_string);

            // ____Get the other statistics____ //
            // get the statistic board below the tweet
            WebElement statistic_board = wait.until(ExpectedConditions.visibilityOfElementLocated(
                  By.cssSelector(".css-175oi2r.r-1kbdv8c.r-18u37iz.r-1oszu61.r-3qxfft.r-n7gxbd" +
                        ".r-2sztyj.r-1efd50x.r-5kkj8d.r-h3s6tt.r-1wtj0ep.r-1igl3o0.r-rull8r.r-qklmqi")
            ));

            // collect its button
            List<WebElement> statistic_buttons = statistic_board.findElements(
                  By.cssSelector(".css-175oi2r.r-18u37iz.r-1h0z5md.r-13awgt0")
            );

            // collect counts from the button list
            List<Integer> statistic_count = statistic_buttons.stream().map(element -> {
                  String text = element.getAttribute("innerText");
                  return text.isEmpty() ? 0 : ConvertTwitterCount.convert(text);
            }).toList();

            // get the info based on the order in the tweet
            int comment_count = statistic_count.get(0);
            int repost_count = statistic_count.get(1);
            int like_count = statistic_count.get(2);
            int bookmark_count = statistic_count.get(3);

            // ____Set the statistics for the tweet____ //
            tweet.setStatistic(view_count, like_count, comment_count, repost_count, bookmark_count);


            /// ____Push tweet to the json object____ ///
            JsonObject tweet_jsonObject = gson.toJsonTree(tweet).getAsJsonObject();
            tweet_data_jsonObject.add(tweet.id, tweet_jsonObject);
            System.out.println("- Info crawled successfully");
            Sleeper.sleep(Constant.SMALL_WAIT_TIME);

            return true;
      }
}

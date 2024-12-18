package data.crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.constant.Constant;
import data.converter.ConvertTwitterCount;
import twitter_element.Tweet;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import data.webtool.Sleeper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * This class is used to crawl the most recent and useful tweets of a user
 */
public class TweetsCrawler extends Crawler {
      /// ____Constant____ ///
      private static final int SCROLL_LENGTH = 2400;
      private static final int FAIL_COUNT_LIMIT = 3;

      /// ____Field____ ///
      private JsonObject tweet_data_jsonObject;
      private TweetEngagementCrawler tweet_engagement_crawler;
      private JavascriptExecutor js_executor;

      /// ____Constructor____ ///
      public TweetsCrawler(WebDriver driver, Gson gson, JsonObject target_jsonObject,
                           JsonObject user_data_jsonObject, JsonObject tweet_data_jsonObject) {
            super(driver, gson, target_jsonObject);

            this.tweet_data_jsonObject = tweet_data_jsonObject;
            this.tweet_engagement_crawler = new TweetEngagementCrawler
                  (driver, gson, target_jsonObject, user_data_jsonObject);
            this.js_executor = (JavascriptExecutor) driver;
      }


      /// ____Method____ ///
      @Override
      public boolean crawl() {
            System.out.println("/// Inspecting tweets ///");

            /*    The first loop is used to extract the urls from satisfying divs
            (obviously after a statistic check).
                  By the end, at most Constant.TWEET_COUNT_LIMIT tweets will be taken.

                  The second loop navigate to each tweet url and crawl its engagement data.
            */

            Set<String> tweet_url_set = new LinkedHashSet<>();
            int fail_count = 0;
            for (int tweet_count = 0; tweet_count < Constant.TWEET_COUNT_LIMIT;) {
                  /// Getting the cell div list
                  List<WebElement> cell_divs = driver.findElements(By.cssSelector(
                        "div[data-testid='primaryColumn'] div[data-testid='cellInnerDiv']"
                  ));

                  /// Loop for cell div
                  boolean at_least = false;
                  for (WebElement inspected_div: cell_divs) {
                        String url;
                        String author;

                        // check if the div is a tweet
                        try {
                              WebElement tweet = inspected_div.findElement(By.cssSelector(
                                    "article[data-testid='tweet']"
                              ));
                              url = inspected_div.findElement(By.xpath
                                    (".//a[contains(@href, '/status/')]"
                              )).getAttribute("href");
                              author = inspected_div.findElement(By.cssSelector(
                                    ".css-175oi2r.r-18u37iz.r-1wbh5a2.r-1ez5h0i"
                              )).getAttribute("innerText");
                              author = author.substring(0, author.indexOf('\n'));
                        } catch (Exception e) {
                              continue;
                        }

                        // check if the tweet is spotted before
                        if (tweet_url_set.contains(url)) continue;
                        // check if it is a repost
                        if (!author.equals(target_jsonObject.get("handle").getAsString())) continue;

                        System.out.println("/// A new tweet is spotted");

                        // check comment count
                        String reply = inspected_div.findElement(By.cssSelector("button[data-testid='reply']"))
                              .getAttribute("innerText");
                        int comment_count = reply.isEmpty() ? 0 : ConvertTwitterCount.convert(reply);
                        if (comment_count < Constant.MIN_COMMENT) {
                              System.out.println("- TweetUserRole reply less than " + Constant.MIN_COMMENT +
                                    ". Omit tweet\n");
                              continue;
                        }

                        // check repost count
                        String repost = inspected_div.findElement(By.cssSelector("button[data-testid='retweet']"))
                              .getAttribute("innerText");
                        int repost_count = repost.isEmpty() ? 0 : ConvertTwitterCount.convert(repost);

                        // check like count
                        String like = inspected_div.findElement(By.cssSelector("button[data-testid='like']"))
                              .getAttribute("innerText");
                        int like_count = like.isEmpty() ? 0 : ConvertTwitterCount.convert(like);
                        if (like_count < Constant.MIN_LIKE) {
                              System.out.println("- TweetUserRole like less than " + Constant.MIN_LIKE +
                                    ". Omit tweet\n");
                              continue;
                        }

                        // check like count
                        String view = inspected_div.findElement(By.cssSelector(
                              "a[role='link'].css-175oi2r.r-1777fci.r-bt1l66.r-bztko3.r-lrvibr.r-1ny4l3l.r-1loqt21"
                        )).getAttribute("innerText");
                        int view_count = view.isEmpty() ? 0 : ConvertTwitterCount.convert(view);
                        if (view_count < Constant.MIN_VIEW) {
                              System.out.println("- TweetUserRole view less than " + Constant.MIN_VIEW +
                                    ". Omit tweet\n");
                              continue;
                        }

                        /// TweetUserRole is qualified
                        System.out.println("- TweetUserRole satisfied");
                        at_least = true;

                        // create a new tweet instance
                        Tweet tweet;
                        String id = url.substring(url.indexOf("/status/") + 8);    // extract the id
                        tweet = new Tweet(id);

                        // add tweet info
                        tweet.setInfo(url, author);

                        // add tweet statistic
                        tweet.setStatistic(view_count, like_count, comment_count, repost_count);

                        // push the tweet into tweet_data
                        JsonObject tweet_jsonObject = gson.toJsonTree(tweet).getAsJsonObject();
                        tweet_data_jsonObject.add(tweet.id, tweet_jsonObject);

                        // finish
                        System.out.println("- Info crawled successfully\n");

                        tweet_url_set.add(url);
                        tweet_count++;

                        if (tweet_count == Constant.TWEET_COUNT_LIMIT) break;

                  }


                  /// Check for failure
                  if (!at_least) {
                        System.out.println("/// A fail happened");
                        fail_count++;
                        if (fail_count == FAIL_COUNT_LIMIT) break;
                  } else {
                        fail_count = 0;
                  }


                  /// Prepare for the next div
                  js_executor.executeScript("window.scrollBy(0, " + SCROLL_LENGTH + ");");
                  Sleeper.sleep(Constant.BIG_WAIT_TIME);
            }

            System.out.println(tweet_url_set.size());

            /*
                  The second loop navigate to each tweet and crawl the tweet engagement
             */
            System.out.println("/// Crawl adjacency of the tweet list /// \n");
            for (String url: tweet_url_set) {
                  System.out.println("/// Crawl tweet: " + url);

                  driver.navigate().to(url);
                  Sleeper.sleep(Constant.BIG_WAIT_TIME);

                  tweet_engagement_crawler.crawl();
            }

            /// ____Announce the crawl is successful____ ///
            System.out.println("/// Tweets extracted successfully");
            return true;
      }
}

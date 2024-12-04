package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import constant.TimeConstant;
import data.Crawler;
import data.info_crawler.TweetInfoCrawler;
import data.util.PageWait;
import data.util.Sleeper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class OneTweetCrawler extends Crawler {
      private WebElement tweet_webElement;
      private JsonObject tweet_jsonObject;
      private JsonObject user_jsonObject;

      public OneTweetCrawler(WebDriver driver, Gson gson,
                           JsonObject user_jsonObject, JsonObject tweet_jsonObject) {
            super(driver, gson);
            this.user_jsonObject = user_jsonObject;
            this.tweet_jsonObject = tweet_jsonObject;
      }

      @Override
      public void navigate () {
            tweet_webElement.click();
            Sleeper.sleep(TimeConstant.MEDIUM_WAIT_TIME);
      }

      @Override
      public void crawl () {
            TweetInfoCrawler tweet_statistic_crawler
                  = new TweetInfoCrawler(driver, gson, tweet_jsonObject);
            CommentCrawler comment_crawler
                  = new CommentCrawler(driver, gson, user_jsonObject);
            RepostCrawler repost_crawler
                  = new RepostCrawler(driver, gson, user_jsonObject, tweet_webElement);




            Sleeper.sleep(TimeConstant.MEDIUM_WAIT_TIME);
      }

      public void navigateBack () {
            driver.navigate().back();
            Sleeper.sleep(TimeConstant.MEDIUM_WAIT_TIME);
      }

      public void setTweet (WebElement tweet_webElement) {
            this.tweet_webElement = tweet_webElement;
      }
}

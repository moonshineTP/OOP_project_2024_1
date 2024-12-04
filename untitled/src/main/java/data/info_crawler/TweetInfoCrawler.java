package data.info_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import data.Crawler;
import graph_element.Tweet;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class TweetInfoCrawler extends Crawler {
      private WebElement tweet_webElement;
      public TweetInfoCrawler(WebDriver driver, Gson gson, JsonObject tweet_jsonObject) {
            super(driver, gson, tweet_jsonObject);
      }

      @Override
      public void navigate () {

      }

      @Override
      public void crawl () {
            String url = driver.getCurrentUrl();
            String id = url.substring(url.indexOf("/status/") + 8); // 8 is the length of "/status/"


            Tweet tweet = new Tweet(id);
//            tweet.setInfo(url, author, timestamp);
//            tweet.setStatistic(view_count, like_count, comment_count, repost_count, bookmark_count);

            JsonObject tweet_jsonObject = gson.toJsonTree(tweet).getAsJsonObject();
            target_jsonObject.add(id, tweet_jsonObject);

            System.out.println("Tweet info crawled successfully!");
      }
}

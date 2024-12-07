package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.openqa.selenium.WebDriver;

import data.constant.Constant;
import data.Crawler;
import data.util.Sleeper;


/**
 * This class is used to crawl all the data of one single tweet, including some statistics and the
 * users reposted and commented on.
 */

public class TweetCrawler extends Crawler {
      /// ________Field________ ///
      private JsonObject user_data_jsonObject;
      private JsonObject tweet_data_jsonObject;


      /// ________Constructor________ ///
      public TweetCrawler(WebDriver driver, Gson gson, JsonObject target_jsonObject,
                          JsonObject user_data_jsonObject, JsonObject tweet_data_jsonObject) {
            super(driver, gson, target_jsonObject);
            this.user_data_jsonObject = user_data_jsonObject;
            this.tweet_data_jsonObject = tweet_data_jsonObject;
      }


      /// ________Method_________ ///
      @Override
      public boolean crawl () {
            /// Initialize crawlers
            TweetInfoCrawler tweet_info_crawler
                  = new TweetInfoCrawler(driver, gson, target_jsonObject, tweet_data_jsonObject);
            TweetEngagementCrawler tweet_engagement_crawler
                  = new TweetEngagementCrawler(driver, gson, target_jsonObject, user_data_jsonObject);

            /// Crawl tweet info
            tweet_info_crawler.crawl();

            /// Crawl tweet engagements
            tweet_engagement_crawler.crawl();

            /// Finish
            System.out.println("/// Tweet crawled successfully ///");
            System.out.println();
            Sleeper.sleep(Constant.SMALL_WAIT_TIME);
            return true;
      }
}

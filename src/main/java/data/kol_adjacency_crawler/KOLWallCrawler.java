package data.kol_adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.Sleeper;
import org.openqa.selenium.WebDriver;

import data.Crawler;
import data.constant.Constant;

/**
 * This class is used to crawls all the data of a user's wall
 */

public class KOLWallCrawler extends Crawler {
      /// ____Field____ ///
      private JsonObject user_data_jsonObject;
      private JsonObject tweet_data_jsonObject;


      /// ____Constructor____ ///
      public KOLWallCrawler(WebDriver driver, Gson gson,
                            JsonObject user_data_jsonObject, JsonObject tweet_data_jsonObject)
      {
            super(driver, gson);
            this.user_data_jsonObject = user_data_jsonObject;
            this.tweet_data_jsonObject = tweet_data_jsonObject;
      }


      /// ____Method____ ///
      @Override
      public void navigate() {
            System.out.println("/// ____Navigate to " + target_jsonObject.get("handle").getAsString() + "____ ///");

            String url = target_jsonObject.get("url").getAsString();
            driver.navigate().to(url);

            Sleeper.sleep(Constant.BIG_WAIT_TIME);
      }

      @Override
      public boolean crawl() {
            System.out.println("/// ____Crawl adjacency of " + target_jsonObject.get("handle").getAsString() + "____ ///");
            System.out.println("-----------------------------------------------");
            System.out.println();

            /// Set up crawlers
            JsonObject kol_map_jsonObject = user_data_jsonObject.getAsJsonObject("KOL");
            FollowingCrawler follow_crawler = new FollowingCrawler
                  (driver, gson, target_jsonObject, kol_map_jsonObject);
            TweetsCrawler tweets_crawler = new TweetsCrawler
                  (driver, gson, target_jsonObject, user_data_jsonObject, tweet_data_jsonObject);

            /// Crawl following KOL
//            follow_crawler.navigate();
//            follow_crawler.crawl();
//            follow_crawler.navigateBack();

            /// Crawl tweets
            tweets_crawler.crawl();

            System.out.println("/// ____KOL crawled successfully____ ///\n");
            return true;
      }

      public void setTarget (JsonObject kol_jsonObject) {
            this.target_jsonObject = kol_jsonObject;
      }
}

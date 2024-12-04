package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.util.Sleeper;
import org.openqa.selenium.WebDriver;

import data.Crawler;
import constant.TimeConstant;


public class KOLWallCrawler extends Crawler {
      private JsonObject user_jsonObject;
      private JsonObject tweet_jsonObject;


      /// ____Constructor____ ///
      public KOLWallCrawler(WebDriver driver, Gson gson,
                            JsonObject user_jsonObject, JsonObject tweet_jsonObject)
      {
            super(driver, gson);
            this.user_jsonObject = user_jsonObject;
            this.tweet_jsonObject = tweet_jsonObject;
      }


      /// ____Method____ ///
      @Override
      public void navigate() {
            String url = target_jsonObject.get("url").getAsString();
            System.out.println(STR."/// Navigate to \{target_jsonObject.get("handle").getAsString()} ///");
            driver.navigate().to(url);
            Sleeper.sleep(TimeConstant.BIG_WAIT_TIME);
      }

      @Override
      public void crawl() {
            System.out.println(STR."/// Crawl adjacency of \{target_jsonObject.get("handle").getAsString()} ///");

            /// Set up crawlers
            JsonObject kol_map_jsonObject = user_jsonObject.getAsJsonObject("KOL");
            FollowingKOLCrawler follow_crawler = new FollowingKOLCrawler
                  (driver, gson, target_jsonObject, kol_map_jsonObject);
            TweetsCrawler tweets_crawler = new TweetsCrawler
                  (driver, gson, target_jsonObject, user_jsonObject, tweet_jsonObject);

            /// Crawl following KOL
//            follow_crawler.navigate();
//            follow_crawler.crawl();
//            follow_crawler.navigateBack();

            /// Crawl tweets
            tweets_crawler.crawl();

            System.out.println("/// Crawl successfully! ///");
            System.out.println();
      }

      public void setTarget (JsonObject kol_jsonObject) {
            this.target_jsonObject = kol_jsonObject;
      }
}

package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.google.gson.JsonPrimitive;
import constant.TimeConstant;
import data.util.*;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import data.FilePath;
import data.Crawler;

/**
 * The class is used to crawl all the tweets data and build the KOL-KOL/KOL-Tweet adjacency
 * The adjacency is later used for the PageRank Algorithm
 */

public class KOL_TweetAdjacencyCrawler extends Crawler {
      /// ____Field____ ///
      private List<JsonObject> kol_list_jsonObject;
      private KOLWallCrawler kol_wall_crawler;


      /// ____Constructor____ ///
      public KOL_TweetAdjacencyCrawler(WebDriver driver, Gson gson) {
            super(driver, gson);
      }


      /// ____Main function____ ///
      public static void main (String[] args) throws IOException, InterruptedException {
            /// Get objects
            // get json objects
            JsonObject user_jsonObject = CustomJsonReader.read(FilePath.USER_DATA_FILE_PATH);
            JsonObject tweet_jsonObject = CustomJsonReader.read(FilePath.TWEET_DATA_FILE_PATH);


            /// Set crawler
            // initialize crawler
            WebDriver driver = ChromeSetup.set();
            Gson gson = new Gson();

            KOL_TweetAdjacencyCrawler crawler = new KOL_TweetAdjacencyCrawler(driver, gson);
            crawler.setObjects(user_jsonObject, tweet_jsonObject);
            crawler.setKolListJsonObject(user_jsonObject);

            /// Crawl
            crawler.navigate();
            Sleeper.sleep(TimeConstant.MEDIUM_WAIT_TIME);
            crawler.crawl();

            /// Write data
            CustomJsonWriter.write(user_jsonObject, FilePath.USER_DATA_FILE_PATH);
            CustomJsonWriter.write(tweet_jsonObject, FilePath.TWEET_DATA_FILE_PATH);
      }


      /// ____Helper method____ ///
      public void setObjects(JsonObject user_jsonObject, JsonObject tweet_jsonObject) {
            this.kol_wall_crawler = new KOLWallCrawler
                  (driver, gson, user_jsonObject, tweet_jsonObject);
      }

      public void setKolListJsonObject (JsonObject user_jsonObject) {
            JsonObject kol_map_jsonObject = user_jsonObject.getAsJsonObject("KOL");

            List<JsonObject> kol_list_jsonObject = new LinkedList<>();
            for (String handle: kol_map_jsonObject.keySet()) {
                  kol_list_jsonObject.add(kol_map_jsonObject.get(handle).getAsJsonObject());
            }

            this.kol_list_jsonObject = kol_list_jsonObject;
      }

      @Override
      public void navigate() {
            System.out.println("/// ________Navigate to the login page________ ///");
            System.out.println();
            driver.navigate().to("https://x.com/i/flow/login");
            Registrar.register(driver);
      }

      @Override
      public void crawl () {
            System.out.println("/// ________Start crawling KOL/Tweet Adjacency________ ///");
            System.out.println();
            /// Loop
            for (JsonObject kol_jsonObject: kol_list_jsonObject) {
                  kol_wall_crawler.setTarget(kol_jsonObject);
                  kol_wall_crawler.navigate();
                  kol_wall_crawler.crawl();
            }
      }
}

package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.constant.Constant;
import org.openqa.selenium.WebDriver;

import data.util.*;
import data.Crawler;

import java.io.IOException;


/**
 * The class is used to crawl all the tweets data and build the KOL-KOL/KOL-Tweet adjacency
 * The adjacency is later used for the PageRank Algorithm
 */

public class AdjacencyCrawler extends Crawler {
      /// ____Field____ ///
      String user_data_file_path;
      String tweet_data_file_path;

      /// ____Constructor____ ///
      public AdjacencyCrawler(WebDriver driver, Gson gson, String user_data_file_path,
                              String tweet_data_file_path)
      {
            super(driver, gson);
            this.user_data_file_path = user_data_file_path;
            this.tweet_data_file_path = tweet_data_file_path;
      }

      /// ____Main function____ ///
      public static void main (String[] args) throws IOException, InterruptedException {
            /// Initialize crawler
            WebDriver driver = ChromeSetup.set();
            Gson gson = new Gson();
            AdjacencyCrawler crawler = new AdjacencyCrawler(driver, gson,
                  Constant.USER_DATA_FILE_PATH, Constant.TWEET_DATA_FILE_PATH);

            /// Crawl and write data
            crawler.navigate();
            crawler.crawl();
      }


      /// ____Helper method____ ///
      @Override
      public void navigate() {
            System.out.println("/// ________Navigate to the login page________ ///");
            System.out.println();
            try {
                  driver.navigate().to("https://x.com/i/flow/login");
                  Registrar.register(driver);

            } catch (Exception e) {
                  throw new RuntimeException("Navigate unsuccessfully");
            }

      }

      @Override
      public boolean crawl () {
            System.out.println("/// ________Start crawling KOL/Tweet Adjacency________ ///");
            System.out.println();

            /// Get main JsonObject
            JsonObject user_jsonObject = CustomJsonReader.read(user_data_file_path);
            JsonObject tweet_jsonObject = CustomJsonReader.read(tweet_data_file_path);

            /// initialize wall crawler
            KOLWallCrawler kol_wall_crawler = new KOLWallCrawler(driver, gson, user_jsonObject, tweet_jsonObject);

            /// Loop
            JsonObject kol_map_jsonObject = user_jsonObject.getAsJsonObject("KOL");
            int kol_count = 0;

            for (String handle: kol_map_jsonObject.keySet()) {
                  // set target for wall crawler
                  kol_wall_crawler.setTarget(kol_map_jsonObject.get(handle).getAsJsonObject());

                  // navigate
                  kol_wall_crawler.navigate();

                  // crawl and write the file
                  kol_wall_crawler.crawl();
                  kol_count++;

                  // write the data
                  CustomJsonWriter.write(user_jsonObject, user_data_file_path);
                  CustomJsonWriter.write(tweet_jsonObject, tweet_data_file_path);
                  System.out.println(STR."/// Data written, \{kol_count} KOL(s) crawled____ ///\n\n\n");

                  if (kol_count == Constant.KOL_LIMIT) break;
            }

            System.out.println("/// ________Adjacency crawled successfully________ ///");
            return true;
      }
}

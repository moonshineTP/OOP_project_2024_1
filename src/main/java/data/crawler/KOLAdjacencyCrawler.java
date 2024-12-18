package data.crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.package_config.FilePath;
import data.util.ChromeSetup;
import data.util.Registrar;
import data.package_config.Constant;
import json.CustomJsonReader;
import json.CustomJsonWriter;
import org.openqa.selenium.WebDriver;


/**
 * The class is used to crawl all the tweets data and build the KOL-KOL/KOL-TweetUserRole adjacency
 * The adjacency is later used for the PageRank Algorithm
 */

public class KOLAdjacencyCrawler extends Crawler {
      /// ____Field____ ///
      String user_data_file_path;
      String tweet_data_file_path;

      /// ____Constructor____ ///
      public KOLAdjacencyCrawler(WebDriver driver, Gson gson, String user_data_file_path,
                                 String tweet_data_file_path)
      {
            super(driver, gson);
            this.user_data_file_path = user_data_file_path;
            this.tweet_data_file_path = tweet_data_file_path;
      }

      /// ____Main function____ ///
      public static void main () {
            boolean not_done_yet = true;
            while (not_done_yet) {
                  /// Initialize crawler
                  WebDriver driver = ChromeSetup.set();
                  Gson gson = new Gson();
                  KOLAdjacencyCrawler crawler = new KOLAdjacencyCrawler(driver, gson,
                        FilePath.USER_DATA_FILE_PATH, FilePath.TWEET_DATA_FILE_PATH);

                  /// Crawl and write data
                  try {
                        crawler.navigate();
                        crawler.crawl();
                  } catch (Exception e) {
                        continue;
                  }

                  /// Finish
                  not_done_yet = false;
            }
      }


      /// ____Helper method____ ///
      @Override
      public void navigate() {
            System.out.println("/// ________Navigate to the login page________ ///");
            System.out.println();

            driver.navigate().to("https://x.com/i/flow/login");
            Registrar.register(driver);
      }

      @Override
      public boolean crawl () {
            System.out.println("/// ________Start crawling KOL/TweetUserRole Adjacency________ ///");
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
                  JsonObject kol_jsonObject = kol_map_jsonObject.get(handle).getAsJsonObject();

                  // check if the kol is crawled or not
                  if (kol_jsonObject.get("crawl_state").getAsBoolean()) continue;

                  System.out.println(kol_jsonObject.get("handle").getAsString());
                  // set target for wall crawler
                  kol_wall_crawler.setTarget(kol_jsonObject);

                  // navigate
                  kol_wall_crawler.navigate();

                  // crawl and write the file
                  kol_wall_crawler.crawl();

                  // update counter
                  kol_count++;
                  //update crawl state
                  kol_jsonObject.addProperty("crawl_state", true);

                  // write the data
                  CustomJsonWriter.write(user_jsonObject, user_data_file_path);
                  CustomJsonWriter.write(tweet_jsonObject, tweet_data_file_path);
                  System.out.println("/// Data written, " + kol_count + " KOL(s) crawled____ ///\n\n\n");

                  // check the break limit
                  if (kol_count == Constant.KOL_COUNT_LIMIT) break;
            }

            /// Finish
            System.out.println("/// ________Adjacency crawled successfully________ ///");
            return true;
      }
}

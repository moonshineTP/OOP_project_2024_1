package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import data.Crawler;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RepostCrawler extends Crawler {
      private WebElement tweet_webELement;

      public RepostCrawler (WebDriver driver, Gson gson,
                            JsonObject user_jsonObject, WebElement tweet_webELement)
      {
            super(driver, gson, user_jsonObject);
            this.tweet_webELement = tweet_webELement;
      }

      @Override
      public void navigate() {

      }

      @Override
      public void crawl () {

      }
}

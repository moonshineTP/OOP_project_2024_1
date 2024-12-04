package data.adjacency_crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import data.Crawler;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CommentCrawler extends Crawler {
      private WebElement tweet_webElement;
      private JsonObject user_jsonObject;

      public CommentCrawler (WebDriver driver, Gson gson,
                             JsonObject user_jsonObject)
      {
            super(driver, gson);
            this.target_jsonObject = user_jsonObject;
      }

      @Override
      public void navigate () {

      }

      @Override
      public void crawl () {

      }
}

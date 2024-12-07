package data.adjacency_crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import data.constant.Constant;
import data.util.Sleeper;

import java.util.List;
import java.util.Set;

class HandleCrawler {
      private final int SCROLL_LENGTH = 2250;
      private WebDriver driver;

      public HandleCrawler (WebDriver driver) {
            this.driver = driver;
      }

      public boolean crawl (Set<String> handle_set) {
            /// Get the current handles loaded in the DOM
            List<WebElement> cur_handle_webElement_list = driver.findElements
                  (By.cssSelector("div[data-testid='primaryColumn'] "
                        + ".css-146c3p1.r-dnmrzs.r-1udh08x.r-3s2u2q.r-bcqeeo.r-1ttztb7.r-qvutc0"
                        + ".r-37j5jr.r-a023e6.r-rjixqe.r-16dba41.r-18u37iz.r-1wvb978"));

            /// Crawl with size check
            int old_size = handle_set.size();
            for (WebElement handle_webElement: cur_handle_webElement_list) {
                  String handle = handle_webElement.getAttribute("innerText");
                  handle_set.add(handle);
            }
            int new_size = handle_set.size();

            /// If the size doesn't change, return false
            if (new_size == old_size) return false;

            /// Scroll to load the next divs
            ((JavascriptExecutor) driver).executeScript(STR."window.scrollBy(0, \{SCROLL_LENGTH});");

            /// Finish
            Sleeper.sleep(Constant.BIG_WAIT_TIME);
            return true;
      }
}

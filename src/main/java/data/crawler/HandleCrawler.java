package data.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import data.constant.Constant;
import data.webtool.Sleeper;

import java.util.List;
import java.util.Set;


/**
 * This class is used to specifically crawl handle in the middle column of the Twitter page's DOM
 */

class HandleCrawler {
      private final int SCROLL_LENGTH = 2400;
      private WebDriver driver;
      private JavascriptExecutor js_executor;

      public HandleCrawler (WebDriver driver) {
            this.driver = driver;
            this.js_executor = (JavascriptExecutor) driver;
      }

      public boolean crawl (Set<String> handle_set) {
            /// Get the current handles loaded in the DOM
            List<WebElement> cellInnerDiv_list = driver.findElements
                  (By.cssSelector("div[data-testid='primaryColumn'] div[data-testid='cellInnerDiv']"));

            /// Crawl with size check
            int old_size = handle_set.size();
            for (WebElement inspected_div: cellInnerDiv_list) {
                  // ignore null div
                  try{
                        if (Integer.parseInt(inspected_div.getAttribute("offsetHeight")) == 0) continue;
                  } catch (Exception e) {
                        break;
                  }

                  // extract handle div, break if the div is a header
                  WebElement handle_div = null;
                  try {
                        handle_div = inspected_div.findElement(
                              By.cssSelector(".css-146c3p1.r-dnmrzs.r-1udh08x.r-3s2u2q.r-bcqeeo.r-1ttztb7"
                                    + ".r-qvutc0.r-37j5jr.r-a023e6.r-rjixqe.r-16dba41.r-18u37iz.r-1wvb978"));
                  } catch (Exception e) {
                        break;
                  }

                  // extract handle
                  String handle = handle_div.getAttribute("innerText");
                  handle_set.add(handle);
            }
            int new_size = handle_set.size();

            /// If the size doesn't change, return false
            if (new_size == old_size) return false;

            /// Scroll to load the next divs
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, " + SCROLL_LENGTH + ");");

            /// Finish
            Sleeper.sleep(Constant.BIG_WAIT_TIME);
            return true;
      }
}

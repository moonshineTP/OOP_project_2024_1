package data.util;

import constant.TimeConstant;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PageWait {
      public static void wait (WebDriverWait wait) {
            try {
                  wait.until( (ExpectedCondition<Boolean>) wd -> {
                        assert wd != null;
                        return ((JavascriptExecutor) wd)
                              .executeScript("return document.readyState")
                              .equals("complete");
                  });
            } catch (Exception e) {
                  throw new RuntimeException("The page takes too long to respond");
            }
      }

      public static void wait (WebDriver driver) {
            wait(new WebDriverWait(driver, Duration.ofMillis(TimeConstant.HUGE_WAIT_TIME)));
      }
}

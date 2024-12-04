package data.util;

import constant.TimeConstant;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * The Registrar class is used to navigate the register menu of X
 */

public class Registrar {
      public static void register(WebDriver driver) {
            System.out.println("/// ________Registering________ ///");
            /// ____Initialize wait and element____ ///
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(TimeConstant.HUGE_WAIT_TIME));
            WebElement element;

            /// Enter email
            try {
                  element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.name("text")
                        )
                  );
                  Thread.sleep(Duration.ofMillis(TimeConstant.SMALL_WAIT_TIME));
                  element.sendKeys("moonshinetpemail@gmail.com\n");
                  System.out.println("Email entered");

            } catch (Exception e) {
                  throw new RuntimeException("Enter email unsuccessfully");
            }


            /// Enter username
            // IMPORTANT NOTE: in some first register trials it is not needed to enter username,
            // but in some later trial it becomes necessary.
            // I absolutely have no idea about the reason behind this behavior,
            try {
                  element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.name("text")
                        )
                  );
                  Thread.sleep(Duration.ofMillis(TimeConstant.SMALL_WAIT_TIME));
                  element.sendKeys("meo_bach_duong\n");
                  System.out.println("Username entered");

            } catch (Exception e) {
                  System.out.println("Username omitted/entered unsuccessfully");
            }

            /// Enter password
            try{
                  element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.name("password")
                        )
                  );
                  Thread.sleep(Duration.ofMillis(TimeConstant.SMALL_WAIT_TIME));
                  element.sendKeys("112358132134gh.\n");
                  System.out.println("Password entered");
            } catch (Exception e) {
                  throw new RuntimeException("Enter password unsuccessfully");
            }

            /// Wait until register completed
            PageWait.wait(wait);

            Sleeper.sleep(TimeConstant.MEDIUM_WAIT_TIME);
            System.out.println("Registered successfully!");
            System.out.println();
      }
}

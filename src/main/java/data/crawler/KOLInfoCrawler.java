package data.crawler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.webtool.ChromeSetup;
import data.webtool.Registrar;
import data.webtool.Sleeper;
import json.CustomJsonReader;
import json.CustomJsonWriter;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import data.constant.Constant;
import data.converter.ConvertTwitterCount;
import twitter_element.KOL;


/**
 *    The KOLTweetCrawler class is used to crawl the list of KOLs from a search page,
 *  crawl their attributes and extract it to the json file
 *    You can change the url to any search page you want
 */

public class KOLInfoCrawler extends Crawler {
      /// ____Main search page____ ///
      private static final String MAIN_URL = "https://x.com/search?q=(blockchain%20OR%20(blockchain%20(%22analyst%22%20OR%20%22expert%22%20OR%20%22enthusiast%22%20OR%20%22advisor%22%20OR%20%22influencer%22%20OR%20%22foundation%22%20OR%20%22institute%22%20OR%20%22network%22%20OR%20%22labs%22%20OR%20%22research%22%20OR%20%22association%22)))%20-%20filter%3Areplies%20min_faves%3A100&src=typed_query&f=user";


      /// ____Constant____ ///
      // limit constants
      private static final int MAX_PROFILE_PER_SESSION = 14;
      private static final int MAX_FAIL = 6;
      private static final int MIN_KOL_FOLLOWER = 10000;
      // div length, calculated in pixels (remember to change to suit your local device)
      private static final int HEADER_HEIGHT = 107;
      private static final int CRAWL_SESSION_SCROLL_LENGTH = 1300;


      /// ____More field____ ///
      JavascriptExecutor js_executor;
      WebDriverWait wait;
      Actions mouse;

      JsonObject user_data;

      /// ____Constructor____ ///
      public KOLInfoCrawler(WebDriver driver, Gson gson, JsonObject target_jsonObject, JsonObject user_data) {
            super(driver, gson, target_jsonObject);
            js_executor = (JavascriptExecutor) driver;
            wait = new WebDriverWait(driver, Duration.ofMillis(Constant.HUGE_WAIT_TIME));
            mouse = new Actions(driver);
            this.user_data = user_data;
      }


      /// ____Main function____ ///
      /*
            This method sets up chromedriver, registers to twitter, crawls the data and
      write it in the json file
       */
      public static void main(String[] args) {
            /// Read the file
            JsonObject user_data = CustomJsonReader.read(Constant.USER_DATA_FILE_PATH);
            JsonObject kol_data = user_data.getAsJsonObject("KOL");

            /// Crawl data
            WebDriver driver = ChromeSetup.set();
            Gson gson = new Gson();
            Crawler crawler = new KOLInfoCrawler(driver, gson, kol_data, user_data);

            crawler.navigate();
            crawler.crawl();
      }


      /// ____Helper method____ ///
      @Override
      public void navigate() {
            System.out.println("/// ________Navigate to the search page________ ///\n");
            try {
                  driver.navigate().to(MAIN_URL);
                  Registrar.register(driver);

            } catch (Exception e) {
                  throw new RuntimeException("Navigate unsuccessfully");
            }

      }

      @Override
      public boolean crawl () {
            System.out.println("/// ________Crawl KOL Attributes________ ///");

            /// Inspect if the page is loaded
            WebElement sample_profile = wait.until(
                  ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div[data-testid='cellInnerDiv']")
                  )
            );

            /// Loop
            /*
                  We use a loop to crawl data until the KOL count get around MAX_KOL
                  Each time, we scroll to load the list of profile divs (tagged "cellInnerDiv")
                  rendered in the screen and crawl them using getProfilesData()
             */
            int previous_KOL_count = 0, contiguous_fail_count = 0;
            int session_count = 1;

            for (int KOL_count = 0; KOL_count < Constant.KOL_COUNT_LIMIT; ) {
                  // print current session info
                  System.out.println(STR."/// Session \{session_count} ///");
                  session_count++;

                  /// Get data
                  // save profile divs rendered currently
                  List<WebElement> profiles = driver.findElements(
                        By.cssSelector("div[data-testid='cellInnerDiv']")
                  );

                  // get profiles data
                  int success_count = crawlProfilesData(profiles);

                  // Write the file
                  CustomJsonWriter.write(user_data, Constant.USER_DATA_FILE_PATH);

                  // update KOL_count
                  KOL_count = target_jsonObject.size();

                  // print search result
                  System.out.println(STR."KOLS retrieved this session: \{success_count}");
                  System.out.println(STR."KOLs retrieved so far: \{KOL_count}");
                  System.out.println();

                  /// Condition check
                  /*
                        If the KOL_count is unchanged, it means that the page has not loaded or
                  all the profiles crawled are not KOL (maybe because the search is too deep)
                        We count it as a fail
                        If the contiguous fail exceeds the MAX_FAIL, break and stop the crawl
                        Else reset the fail count to 0 and update previous_KOL_count to KOL_count
                   */

                  if (previous_KOL_count == KOL_count) { // fail case
                        contiguous_fail_count++;
                        if (contiguous_fail_count == MAX_FAIL) {
                              break;
                        }
                  } else { // successful case
                        previous_KOL_count = KOL_count;
                        contiguous_fail_count = 0;
                  }

                  /// scroll down to load the next divs for the next crawl session
                  js_executor.executeScript(STR."window.scrollBy(0, \{CRAWL_SESSION_SCROLL_LENGTH});");

                  /// Sleep a little bit
                  Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
            }

            System.out.println("/// ________KOL attributes extracted successfully________ ///");
            return true;
      }


      /*
            Method to iterate each profile and crawl data from the profile list.
            Moreover, it returns the number of KOLs extracted successfully
       */
      private int crawlProfilesData(List<WebElement> profiles) {
            int success_count = 0, total_count = 0;

            for (WebElement profile: profiles) {
                  /// Adjust the first profile div
                  if (total_count == 0) {
                        // get the distance from the top of the viewpoint
                        long distance_to_top =
                              (long) js_executor.executeScript("return " +
                                    "arguments[0].getBoundingClientRect().top;", profile);
                        // scroll it to place it right under the header
                        js_executor.executeScript(STR."window.scrollBy(0, \{distance_to_top - HEADER_HEIGHT});");
                  }

                  /// Crawl and push the data of the current profile
                  boolean is_successful;
                  try{
                        is_successful = crawlOneProfileData(profile);
                  } catch (StaleElementReferenceException e) {
                        is_successful = false;
                  }


                  /// Scroll down to render the next profile div list
                  int profile_height = Integer.parseInt(profile.getAttribute("clientHeight"));
                  js_executor.executeScript(STR."window.scrollBy(0, \{profile_height});");

                  /// Update the counter
                  total_count++;
                  if (is_successful) success_count++;

                  /// Check total count to break
                  if(total_count == MAX_PROFILE_PER_SESSION) break; // check the session limit
            }

            /// Write info in stdout
            double success_rate = 100.0f * success_count / total_count;
            System.out.println(STR."Extraction success rate: \{String.format("%.2f", success_rate)} %");

            /// Return success count
            return success_count;
      }


      /*    Method to crawl data of each profile and check if it is a KOL.
            If satisfied, it adds the handle - KOL pair to the current data batch.
            Return true if the profile is a KOL and inserted successfully, else return false.  */

      private boolean crawlOneProfileData(WebElement profile) throws StaleElementReferenceException {
            // find usernameElement
            Sleeper.sleep(Constant.MEDIUM_WAIT_TIME);
            WebElement usernameElement = profile.findElement(
                  By.cssSelector("[class='css-175oi2r r-1wbh5a2 r-dnmrzs r-1ny4l3l r-1loqt21']")
            );

            // trigger hover_card
            mouse.moveToElement(usernameElement).perform();

            // wait the hover_card to be rendered
            WebElement hover_card = wait.until(
                  ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div[data-testid='HoverCard']")
                  )
            );

            /// Collect data
            /*
                  Sometimes the account is private or the hover_card pops up too late,
            so we cant access the follow count
                  Therefore we should use a try-catch and return false if necessary
             */

            // declare data
            String url, username, handle, following_X_count, follower_X_count;
            boolean is_verified;
            int following_count, follower_count;

            // main work
            try {
                  url = usernameElement.getAttribute("href");

                  username = usernameElement.getAttribute("textContent");

                  handle = profile.findElement(
                        By.cssSelector("[class='css-175oi2r r-1awozwy r-18u37iz r-1wbh5a2']")
                  ).getAttribute("textContent");

                  try {
                        // check if the verified icon is present
                        WebElement badge = profile.findElement(
                              By.cssSelector("svg[aria-label='Verified account']")
                        );
                        is_verified = true;
                  } catch (NoSuchElementException e) {
                        is_verified = false;
                  }

                  following_X_count = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.cssSelector(STR."a[href='/\{handle.substring(1)}/following']")
                        )
                  ).getAttribute("textContent");

                  // NOTE: following_X_count has a form of smt like 12.3K Following
                  // so we extract just the first word and parse it to int using ConvertTwitterCount
                  following_count = ConvertTwitterCount.convert(
                        following_X_count.substring(0, following_X_count.indexOf(" "))
                  );

                  follower_X_count = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.cssSelector(STR."a[href='/\{handle.substring(1)}/verified_followers']")
                        )
                  ).getAttribute("textContent");

                  // The same for follower_count
                  follower_count = ConvertTwitterCount.convert(
                        follower_X_count.substring(0, follower_X_count.indexOf(" "))
                  );
            }
            catch (Exception e) {
                  return false;
            }


            /// Check
            if (target_jsonObject.has(handle)) return false;
            if (follower_count < MIN_KOL_FOLLOWER) return false;


            /// Add data
            // create an KOL instance
            KOL kol = KOL.createKOLProfile(handle, url, username,
                  is_verified, following_count, follower_count);
            // convert it to jsonObject
            JsonObject kol_jsonObject = gson.toJsonTree(kol).getAsJsonObject();
            // add the pair to the kol_map
            target_jsonObject.add(handle, kol_jsonObject);


            /// Finish
            return true;
      }
}

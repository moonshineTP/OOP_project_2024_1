package data;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * The KOLDataCrawler class is used to crawl all the data needed and extract it to the txt file
 * Change the url to the search page you want
 */

public class KOLCrawler {
      /// Main page
      private static final String MAIN_URL = "https://x.com/search?q=blockchain%20(%22analyst%22%20OR%20%22expert%22%20OR%20%22enthusiast%22%20OR%20%22advisor%22%20OR%20%22influencer%22%20OR%20%22foundation%22%20OR%20%22institute%22%20OR%20%22network%22%20OR%20%22labs%22%20OR%20%22research%22%20OR%20%22association%22)%20-filter%3Areplies%20min_faves%3A100&src=typed_query&f=user";


      /// ____Constant____ ///
      // limit constants
      private static final int MAX_KOL = 510;
      private static final int MAX_PAST_KOL = 5;
      private static final int MAX_PROFILE = 14;
      private static final int MAX_FAIL = 10;
      private static final int MAX_DATA_LENGTH = 60;
      private static final int MIN_KOL_FOLLOWER = 5000;

      // div length, calculated in pixels
      // remember to change to suit your local device
      private static final int HEADER_HEIGHT = 107;
      private static final int CRAWL_SCROLL_LENGTH = 1300;

      // time constants, all in milliseconds
      private static final int HUGE_WAIT_TIME = 7500;
      private static final int BIG_WAIT_TIME = 2500;
      private static final int MEDIUM_WAIT_TIME = 750;
      private static final int SMALL_WAIT_TIME = 250;
      private static final int TINY_WAIT_TIME = 75;

      // the path should be changed for your local machine
      private static final String TXT_FILE_PATH
            = "C:\\Users\\admin\\IdeaProjects\\OOP_project\\untitled\\src\\main\\java\\data\\raw KOL data.txt";
      private static final String CHROMEDRIVER_PATH
            = "C:\\Users\\admin\\OneDrive\\Máy tính\\chromedriver.exe";

      /// ____Main function____ ///
      /*
            This method set up chrome, registers to twitter, crawls the data and
      save in the txt file
       */
      public static void main(String[] args) throws InterruptedException, IOException {
            /// ____Initialize stuff____ ///
            // set the chromedriver
            System.setProperty("webdriver.chrome.driver", CHROMEDRIVER_PATH);

            // set driver
            WebDriver driver = new ChromeDriver();

            // navigate to twitter
            driver.manage().window().maximize();
            driver.navigate().to(MAIN_URL);

            /// ________Register________ ///
            register(driver);
            Thread.sleep(Duration.ofMillis(HUGE_WAIT_TIME));

            /// ____Crawl user url____ ///
            // open FileWriter and BufferedWriter
            FileWriter file_writer = new FileWriter(TXT_FILE_PATH);
            BufferedWriter buffered_writer = new BufferedWriter(file_writer);

            // ____crawl____ //
            int total_KOL = crawl(driver, buffered_writer);
            buffered_writer.write(STR."Total KOL extracted: \{total_KOL}");

            // close the writer
            buffered_writer.close();
            file_writer.close();
      }


      /// ____Helper method____ ///
      /*
            Method to sign in Twitter.
            The procedure have three steps:
                  - enter email/phone number
                  - enter username
                  - enter password
       */
      private static void register (WebDriver driver) {
            // initialize wait and element
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(HUGE_WAIT_TIME));
            WebElement element;

            // enter email
            try {
                  element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.name("text")
                        )
                  );
                  Thread.sleep(Duration.ofMillis(SMALL_WAIT_TIME));
                  element.sendKeys("moonshinetpemail@gmail.com\n"); //

            } catch (Exception e) {
                  e.printStackTrace();
            }

//            // enter username
//            try {
//                  element = wait.until(
//                        ExpectedConditions.visibilityOfElementLocated(
//                              By.name("text")
//                        )
//                  );
//                  Thread.sleep(Duration.ofMillis(SMALL_WAIT_TIME));
//                  element.sendKeys("meo_bach_duong\n");
//
//            } catch (Exception e) {
//                  e.printStackTrace();
//            }

            // enter password
            try{
                  element = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.name("password")
                        )
                  );
                  Thread.sleep(Duration.ofMillis(SMALL_WAIT_TIME));
                  element.sendKeys("112358132134gh.\n");
            } catch (Exception e) {
                  e.printStackTrace();
            }

            // wait until register completed
            wait.until( (ExpectedCondition<Boolean>) wd -> {
                  assert wd != null;
                  return ((JavascriptExecutor) wd)
                        .executeScript("return document.readyState")
                        .equals("complete");
            });
      }

      /*
            Method to crawl the profile url list from the search with hashtag #blockchain
            Remember to sleep after each scrolling to let the page load and not act like a bot
       */

      private static int crawl (WebDriver driver, BufferedWriter buffered_writer)
            throws InterruptedException
      {
            // js_executor for scrolling
            JavascriptExecutor js_executor = (JavascriptExecutor) driver;

            // list of KOL urls
            String[] KOL_url_list = new String[MAX_KOL];

            // helper variables
            int prev_KOL_count = 0, contiguous_fail_count = 0;


            /// ____Main loop____ ///
            /*
                  The main loop crawl data until the KOL count get near to MAX_KOL
                  MAX_PAST_KOL control the KOL number limit of each crawl attempt
                  Each time, we collect the list of profile divs rendered in the screen
            and crawl all the data using getProfilesData()
             */
            int session_count = 1;
            for (int KOL_count = 0; KOL_count < MAX_KOL - MAX_PROFILE; ) {
                  // crawl profile divs rendered currently
                  List<WebElement> profiles = driver.findElements(
                        By.cssSelector("div[data-testid='cellInnerDiv']")
                  );

                  // get data from profiles and update KOL_count
                  KOL_count += getProfilesData(driver, KOL_url_list, KOL_count,
                        profiles, buffered_writer);


                  /*
                        If the KOL_count is unchanged, it means that the page has not loaded or
                  all the profiles crawled are not KOL (maybe because the search is too deep)
                        We count it as a fail
                        If the contiguous fail exceeds the MAX_FAIL, break and stop the crawl
                        Else reset the fail count to 0 and update prev_KOL_count to KOL_count
                   */

                  if (prev_KOL_count == KOL_count) { // fail case
                        contiguous_fail_count++;
                        if (contiguous_fail_count == MAX_FAIL) {
                              break;
                        }
                  } else { // successful case
                        prev_KOL_count = KOL_count;
                        contiguous_fail_count = 0;
                  }


                  // print search result
                  System.out.println(STR."Session \{session_count}:");
                  session_count++;
                  System.out.println(STR."KOLs retrieved: \{KOL_count}");
                  System.out.println();


                  // scroll down to render the next profile div list
                  js_executor.executeScript(STR."window.scrollBy(0, \{CRAWL_SCROLL_LENGTH});");
                  // sleep a little bit
                  Thread.sleep(2 * SMALL_WAIT_TIME);
            }

            return prev_KOL_count;
      }


      /*
            Method to loop and crawl all the profile from the profile list
            Return the KOL numbers extracted successfully
       */
      private static int getProfilesData(WebDriver driver, String[] KOL_url_list,
            int KOL_count, List<WebElement> profiles, BufferedWriter buffered_writer)
                  throws InterruptedException
      {
            // counter of current successful extraction
            int successful_count = 0;
            int total_count = 0;

            for (WebElement profile: profiles) {
                  Thread.sleep(SMALL_WAIT_TIME);

                  boolean isSuccessful = getOneProfileData(driver, KOL_url_list,
                        KOL_count + successful_count, profile, buffered_writer);

                  total_count++;
                  if (isSuccessful) successful_count++;

                  if(total_count == MAX_PROFILE) break;
            }

            double success_rate = 100.0f * successful_count / total_count;
            System.out.println(STR."Extraction success rate: \{String.format("%.2f", success_rate)} %");
            return successful_count;
      }


      /*
            Method to crawl data of each user, with KOL checking and proper string formatting
            To crawl data, we should first
            The order of data in each line should be:
                  - URL
                  - Username
                  - Handle
                  - Verified state (yes/no)
                  - Following_count
                  - Follower_count
             Return true if the user is a KOL and written successfully, else return false;
       */
      private static boolean getOneProfileData(WebDriver driver, String[] KOL_user_list,
            int KOL_count, WebElement profile, BufferedWriter buffered_writer)
                  throws InterruptedException
      {
            // set js_executor for scrolling down
            JavascriptExecutor js_executor = (JavascriptExecutor) driver;

            // locate the div and get profile div's height
            // This is later use to scroll down the div
            // If unsuccessful, scroll up a little bit
            int profile_height;
            try {
                  profile_height = Integer.parseInt(profile.getAttribute("clientHeight"));
            } catch (Exception e) {
                  js_executor.executeScript(STR."window.scrollBy(0, \{-100});");
                  return false;
            }

            // the profile may be above the viewpoint, we need to scroll it down
            Long distance_to_top = (Long) js_executor
                  .executeScript("return arguments[0].getBoundingClientRect().top;", profile);
            js_executor.executeScript(STR."window.scrollBy(0, \{distance_to_top - HEADER_HEIGHT});");

            // set waiter and mouse
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(HUGE_WAIT_TIME));
            Actions mouse = new Actions(driver);

            // find usernameElement
            Thread.sleep(Duration.ofMillis(MEDIUM_WAIT_TIME));
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


            /// ________Get data________ ///
            // declare data
            String url, username, handle, verification_state, following_count, follower_count;

            /*
                  Sometimes the account is private so we cant access the follow count
            or the hover_card pops up too late, so we should use a try-catch
            and return false if needed
             */

            // get data
            try {
                  // get url
                  url = usernameElement.getAttribute("href");

                  // get username
                  username = usernameElement.getAttribute("textContent");

                  // get handle
                  handle = profile.findElement(
                        By.cssSelector("[class='css-175oi2r r-1awozwy r-18u37iz r-1wbh5a2']")
                  ).getAttribute("textContent");

                  // get verification_state
                  try {
                        // check if the verified icon is present
                        WebElement badge = profile.findElement(
                              By.cssSelector("svg[aria-label='Verified account']")
                        );
                        verification_state = "Yes";
                  } catch (NoSuchElementException e) {
                        verification_state = "No";
                  }


                  // get following_count
                  following_count = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.cssSelector(STR."a[href='/\{handle.substring(1)}/following']")
                        )
                  ).getAttribute("textContent");

                  // get follower_count
                  follower_count = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                              By.cssSelector(STR."a[href='/\{handle.substring(1)}/verified_followers']")
                        )
                  ).getAttribute("textContent");
            }
            catch (Exception e) {
                  // Scroll down to exactly one div
                  js_executor.executeScript(STR."window.scrollBy(0, \{profile_height});");
                  return false;
            }


            /// ________Checking_________ ///
            // check if the URL has been searched before
            boolean existed = false;

            for (int past_url_idx = Math.max(0, KOL_count - MAX_PAST_KOL);
                  past_url_idx < KOL_count; past_url_idx++)
            {
                  if (url.equals(KOL_user_list[past_url_idx])) {
                        existed = true;
                        break;
                  }
            }

            // if existed, return false immediately;
            if (existed) {
                  // Scroll down to exactly one div
                  js_executor.executeScript(STR."window.scrollBy(0, \{profile_height});");
                  return false;
            }

            // Check KOL criteria
            // Note that the number of follower count is shown in abbreviated form (like 1.2M, 34K)
            // So we need to parse that into int using parseFollowerCount()
            if (parseFollowerCount(follower_count) < MIN_KOL_FOLLOWER) {
                  // Scroll down to exactly one div
                  js_executor.executeScript(STR."window.scrollBy(0, \{profile_height});");
                  return false;
            }

            /// ________Success find, do the remaining stuffs________ ///
            // insert url to the url list
            KOL_user_list[KOL_count] = url;

            // ____Write data____//
            try{
                  // write stuff in one line
                  buffered_writer.write(customStringFormatting(url));
                  buffered_writer.write(customStringFormatting(username));
                  buffered_writer.write(customStringFormatting(handle));
                  buffered_writer.write(customStringFormatting(verification_state));
                  buffered_writer.write(customStringFormatting(following_count));
                  buffered_writer.write(customStringFormatting(follower_count));

                  // get to the next line
                  buffered_writer.newLine();
            } catch (IOException e) {
                  throw new RuntimeException();
            }

            // Scroll down to exactly one div
            js_executor.executeScript(STR."window.scrollBy(0, \{profile_height});");
            return true;
      }


      /*
            Method to create a custom data string formatting with proper space padding
       */
      private static String customStringFormatting (String str) {
            return String.format(STR."%-\{MAX_DATA_LENGTH}s", str);
      }


      /*
            Method to extract the follower number, parse and expand abbreviated follower counts
       */
      public static int parseFollowerCount(String follower_count) {
            int space_idx = follower_count.indexOf(' ');

            String follower_num = follower_count.substring(0, space_idx);
            if (follower_num.endsWith("K")) {
                  return (int)(Double.parseDouble(follower_num.replace("K", "")) * 1_000);
            } else if (follower_num.endsWith("M")) {
                  return (int)(Double.parseDouble(follower_num.replace("M", "")) * 1_000_000);
            } else if (follower_num.endsWith("B")) {
                  return (int)(Double.parseDouble(follower_num.replace("B", "")) * 1_000_000_000);
            } else {
                  return Integer.parseInt(follower_num.replace(",", ""));
            }
      }
}

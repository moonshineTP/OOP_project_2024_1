package data.constant;

/**
 * This class defines constants used throughout the file
 */

public class Constant {
      // file path
      public static final String CHROMEDRIVER_PATH = "untitled/tool/chromedriver.exe";
      public static final String USER_DATA_FILE_PATH = "untitled/data/User_data.json";
      public static final String TWEET_DATA_FILE_PATH = "untitled/data/Tweet_data.json";

      // time constants, all in milliseconds
      public static final int HUGE_WAIT_TIME = 7000;
      public static final int BIG_WAIT_TIME = 2300;
      public static final int MEDIUM_WAIT_TIME = 700;
      public static final int SMALL_WAIT_TIME = 230;

      // Epoch timestamp
      public static final String START_TIMESTAMP = "12:00 AM · Oct 01, 2024";

      // Object count limits
      public static final int KOL_COUNT_LIMIT = 200;
      public static final int TWEET_COUNT_LIMIT = 10;

      public static final int MIN_VIEW = 2500;
      public static final int MIN_LIKE = 50;
      public static final int MIN_COMMENT = 5;
}

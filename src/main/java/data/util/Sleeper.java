package data.util;

import java.time.Duration;

public class Sleeper {
      public static void sleep (int sleep_time_millis) {
            try {
                  Thread.sleep(Duration.ofMillis(sleep_time_millis));
            } catch (InterruptedException e) {
                  e.printStackTrace();
                  throw new RuntimeException("Sleep unsuccessfully");
            }
      }
}

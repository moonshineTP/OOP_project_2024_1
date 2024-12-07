package data.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static data.constant.Constant.START_TIMESTAMP;

/**
 *    This class is used to calculate and return the difference (in minutes) from the timestamp
 *    and the epoch timestamp defined in constant.Constant
 */
public class ConvertToMinutesSinceStart {
      public static long convert(String timestamp) {
            String processed_timestamp = timestamp.replace("Last edited", "").trim();
            // Define the formatter based on the input format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a · MMM d, yyyy");

            // Parse the timestamps into LocalDateTime objects
            LocalDateTime inputTime = LocalDateTime.parse(processed_timestamp, formatter);
            LocalDateTime startTime = LocalDateTime.parse(START_TIMESTAMP, formatter);

            // Calculate and return the difference (in minutes)
            return ChronoUnit.MINUTES.between(startTime, inputTime);
      }
}

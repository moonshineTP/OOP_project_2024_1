package converter;

import constant.TimeConstant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 *    This class is used to return the difference (in minutes) from the timestamp
 *    and the start timestamp defined in TimeConstant
 */

public class ConvertToMinutesSinceStart {
      public static long convert(String timestamp) {
            // Define the formatter based on the input format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a · MMM d, yyyy");

            // Parse the timestamps into LocalDateTime objects
            LocalDateTime inputTime = LocalDateTime.parse(timestamp, formatter);
            LocalDateTime startTime = LocalDateTime.parse(TimeConstant.START_TIMESTAMP, formatter);

            // Calculate and return the difference (in minutes)
            return ChronoUnit.MINUTES.between(startTime, inputTime);
      }
}

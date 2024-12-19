package data.util;

/**
 * This class is used to convert Twitter number format to int
 */
public class ConvertTwitterCount {
      public static int convert (String number) {
            if (number.endsWith("K"))
                  return (int) (Double.parseDouble(number.replace("K", "")) * 1_000);

            if (number.endsWith("M"))
                  return (int) (Double.parseDouble(number.replace("M", "")) * 1_000_000);

            if (number.endsWith("B"))
                  return (int) (Double.parseDouble(number.replace("B", "")) * 1_000_000_000);

            return Integer.parseInt(number.replace(",", ""));
      }
}

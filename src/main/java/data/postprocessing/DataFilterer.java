package data.postprocessing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import data.package_config.FilePath;
import json.CustomJsonReader;
import json.CustomJsonWriter;

import java.util.Iterator;
import java.util.Map;


public class DataFilterer {
      public static void main () {
            /// Example code, change as your will
            DataFilterer filterer = new DataFilterer();
            filterer.filterKOLByFollower(20000);
      }

      public void filterKOLByFollower(int min_follower) {
            JsonObject user_data = CustomJsonReader.read(FilePath.USER_DATA_FILE_PATH);
            JsonObject kol_data = user_data.getAsJsonObject("KOL");
            Iterator<Map.Entry<String, JsonElement>> iterator = kol_data.entrySet().iterator();
            while (iterator.hasNext()) {
                  Map.Entry<String, JsonElement> entry = iterator.next();
                  JsonObject kol = entry.getValue().getAsJsonObject();
                  int follower_count = kol.get("follower_count").getAsInt();
                  if (follower_count < min_follower) {
                        iterator.remove();  // Safe removal during iteration
                  }
            }

            System.out.println("KOL(s) after filtering: " + kol_data.size());

            CustomJsonWriter.write(user_data, FilePath.USER_DATA_FILE_PATH);
      }

      public void filterKOLByVerifiedBadge () {
            JsonObject kol_data = CustomJsonReader.read(FilePath.USER_DATA_FILE_PATH).getAsJsonObject("KOL");
            for (String kol_handle: kol_data.keySet()) {
                  boolean is_verified = kol_data.getAsJsonObject(kol_handle).get("is_verified").getAsBoolean();
                  if (!is_verified) kol_data.remove(kol_handle);
            }
      }
}

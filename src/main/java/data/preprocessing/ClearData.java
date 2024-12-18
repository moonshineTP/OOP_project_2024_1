package data.preprocessing;

import com.google.gson.JsonObject;
import data.package_config.FilePath;
import json.CustomJsonReader;
import json.CustomJsonWriter;

public class ClearData {
      public static void main () {
            ClearData clearData = new ClearData();
            clearData.clearNonKOLData();
            clearData.clearTweetData();
            System.out.println("Data cleared!");
      }

      private void clearKOLData () {
            // create a new JsonObject
            JsonObject user_data_jsonObject = CustomJsonReader.read(FilePath.USER_DATA_FILE_PATH);
            user_data_jsonObject.remove("KOL");
            user_data_jsonObject.add("KOL", new JsonObject());
            // overwrite the file
            CustomJsonWriter.write(user_data_jsonObject, FilePath.USER_DATA_FILE_PATH);
      }

      private void clearNonKOLData () {
            // create a new JsonObject
            JsonObject user_data_jsonObject = CustomJsonReader.read(FilePath.USER_DATA_FILE_PATH);
            user_data_jsonObject.remove("Non-KOL");
            user_data_jsonObject.add("Non-KOL", new JsonObject());
            // overwrite the file
            CustomJsonWriter.write(user_data_jsonObject, FilePath.USER_DATA_FILE_PATH);
      }

      private void clearTweetData () {
            // create a new JsonObject
            JsonObject tweet_data = new JsonObject();
            // overwrite the file
            CustomJsonWriter.write(tweet_data, FilePath.TWEET_DATA_FILE_PATH);
      }
}

package json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;

public class CustomJsonReader {
      public static JsonObject read (String json_file_path) {
            try{
                  FileReader reader = new FileReader(json_file_path);
                  JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                  reader.close();

                  return jsonObject;
            } catch (IOException e) {
                  e.printStackTrace();
                  throw new RuntimeException(STR."File \{json_file_path} read unsuccessfully");
            }
      }
}

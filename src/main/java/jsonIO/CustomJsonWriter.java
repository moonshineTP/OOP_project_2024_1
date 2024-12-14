package jsonIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.FileWriter;

public class CustomJsonWriter {
      public static final int INDENT = 6;

      public static void write (JsonObject json_object, String json_file_path) {
            try {
                  // set gson with pretty printing
                  Gson pretty_gson = new GsonBuilder().setPrettyPrinting().create();

                  // set writer
                  JsonWriter writer = new JsonWriter(new FileWriter(json_file_path));
                  writer.setIndent(" ".repeat(INDENT));

                  // write the file
                  pretty_gson.toJson(json_object, writer);

                  // close the writer
                  writer.close();
            } catch (Exception e) {
                  e.printStackTrace();
                  throw new RuntimeException(e);
            }

      }
}

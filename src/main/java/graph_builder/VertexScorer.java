package graph_builder;

import com.google.gson.JsonObject;

public class VertexScorer {
      private final static float ENGAGE_COEFFICIENT = 10;
      /// Score vertices
      float scoreKOL (JsonObject kol) {
            return kol.get("follower_count").getAsInt();
      }

      float scoreNonKOL () {
            return 300;
      }

      float scoreTweet (JsonObject tweet) {
            int views = tweet.get("view_count").getAsInt();
            int likes = tweet.get("like_count").getAsInt();
            int comments = tweet.get("comment_count").getAsInt();
            int reposts = tweet.get("repost_count").getAsInt();

            int engages = likes + comments+ reposts;
            return (float) (1.0f * views * Math.pow(1.0f * views / (views - engages), ENGAGE_COEFFICIENT));
      }
}

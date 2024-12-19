package graph_builder;

import com.google.gson.JsonObject;


/**
 * This method is used to score vertex types, which method can be implemented at will
 */

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

            // Trọng số cho các loại tương tác
            float weightLike = 0.5f;
            float weightComment = 1.0f;
            float weightRepost = 1.5f;

            // Engagement Score với trọng số
            float weightedEngagement = weightLike * likes + weightComment * comments + weightRepost * reposts;

            // Tỷ lệ tương tác (Engagement Rate)
            float engagementRate = weightedEngagement / views;

            // Công thức tính điểm (chuẩn hóa với log để tránh outliers)
            return (float) (views * Math.log(1 + weightedEngagement) * engagementRate);
      }
}

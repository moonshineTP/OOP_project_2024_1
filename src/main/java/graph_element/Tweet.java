package graph_element;

public class Tweet {
      /// Identifier attributes
      public String id;

      /// Info attributes
      public String url;
      public String author;
      public String timestamp;

      /// Statistic attributes
      public int view_count;
      public int like_count;
      public int comment_count;
      public int repost_count;
      public int bookmark_count;

      public Tweet (String id) {
            this.id = id;
      }

      public void setInfo (String url, String author, String timestamp) {
            this.url = url;
            this.author = author;
            this.timestamp = timestamp;
      }

      public void setStatistic (int view_count, int like_count, int comment_count,
                                int repost_count, int bookmark_count)
      {
            this.view_count = view_count;
            this.like_count = like_count;
            this.comment_count = comment_count;
            this.repost_count = repost_count;
            this.bookmark_count = bookmark_count;
      }
}

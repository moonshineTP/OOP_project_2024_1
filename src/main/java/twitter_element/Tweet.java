package twitter_element;

/**
 * This class defines the basic information of a Tweet
 */


public class Tweet {
      /// Identifier attributes
      public String id;

      /// Info attributes
      public String url;
      public String author;

      /// Statistic attributes
      public int view_count;
      public int like_count;
      public int comment_count;
      public int repost_count;



      /// Constructor
      public Tweet (String id) {
            this.id = id;
      }



      /// Setter methods
      public void setInfo (String url, String author) {
            this.url = url;
            this.author = author;
      }

      public void setStatistic (int view_count, int like_count, int comment_count,
                                int repost_count)
      {
            this.view_count = view_count;
            this.like_count = like_count;
            this.comment_count = comment_count;
            this.repost_count = repost_count;
      }
}

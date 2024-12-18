package twitter_element;

import java.util.LinkedList;

/**
 * This class defines the basic information of an user
 */

public class User{
      /// Identifier attributes
      public String handle;

      /// Engagement attributes
      public LinkedList<Long> quote_tweet_id_list;
      public LinkedList<Long> repost_tweet_id_list;
      public LinkedList<Long> comment_tweet_id_list;



      /// Constructor
      public User (String handle) {
            this.handle = handle;
            this.quote_tweet_id_list = new LinkedList<>();
            this.repost_tweet_id_list = new LinkedList<>();
            this.comment_tweet_id_list = new LinkedList<>();
      }
}

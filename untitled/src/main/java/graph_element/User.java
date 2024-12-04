package graph_element;

import java.util.LinkedList;

public class User{
      /// Identifier attributes
      public String handle;

      /// Adjacency attributes
      public LinkedList<Long> comment_tweet_id_list;
      public LinkedList<Long> repost_tweet_id_list;

      /// Constructor
      public User (String handle) {
            this.handle = handle;
            this.comment_tweet_id_list = new LinkedList<>();
            this.repost_tweet_id_list = new LinkedList<>();
      }
}

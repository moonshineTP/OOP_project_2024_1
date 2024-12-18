package twitter_element;

import java.util.LinkedList;

/**
 * This class defines the basic information of a KOL by extending more attributes to User
 */

public class KOL extends User{
      /// More info attributes
      public String url;
      public String username;

      /// Statistic attributes
      public boolean is_verified;
      public int following_count;
      public int follower_count;

      /// More adjacency attribute
      public LinkedList<Long> following_kol_handle_list;

      /// Crawl state
      public boolean crawl_state = false;

      /// Constructor
      public KOL (String handle, String url, String username,
            boolean is_verified, int following_count, int follower_count)
      {
            super(handle);
            this.url = url;
            this.username = username;
            this.is_verified = is_verified;
            this.following_count = following_count;
            this.follower_count = follower_count;
            this.following_kol_handle_list = new LinkedList<>();
      }

      /// Build method
      public static KOL createKOLProfile (String handle, String url, String username,
            boolean is_verified, int following_count, int follower_count)
      {
            return new KOL (handle, url, username, is_verified, following_count, follower_count);
      }
}

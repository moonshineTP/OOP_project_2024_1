Interface: graph_element.KOL,
*) Field:
      -) Raw KOLdata
                  + handle                :     String            (identifying user, search)
                  + username              :     String            (search)
                  + is_verified           :     boolean           (search)
                  + follower_count        :     int               (use for pagerank)
                  + following_count       :     int               (use for pagerank)
                  + tweets                :     ArrayList<graph_element.Tweet>  (for pagerank weight)
                  + replies               :     ArrayList<Reply>  (for pagerank weight)

      -) Pagerank KOLdata


Inherited classes: graph_element.KOL, Follower

Interface: graph_element.Tweet
Interface: Comment
Class: PagerankMatrix


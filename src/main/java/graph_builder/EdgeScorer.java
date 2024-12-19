package graph_builder;

/**
 * This class is used to classify edge type and its weight.
 * Based on that, it has methods to score each edge type.
 */
public class EdgeScorer {
      public float scoreKOLEdge (KOLActivityType type) {
            return type.score;
      }

      public float scoreTweetAuthorEdge () {
            return TweetUserRole.AUTHOR.ratio();
      }

      public float scoreTweetCommenterEdge(CommenterType type) {
            return TweetUserRole.COMMENT.ratio() * type.score;
      }


      public float scoreNonKOLEdge (NonKOLActivityType type) {
            return type.score;
      }


      public enum KOLActivityType {
            FOLLOW(10),
            TWEET(5),
            QUOTE(4),
            REPOST(2),
            COMMENT(1);

            private final int score;

            KOLActivityType (int score) {
                  this.score = score;
            }

            public int score () {
                  return score;
            }
      }


      public enum TweetUserRole {
            AUTHOR(1.0f),
            COMMENT(0.5f);

            private final float score;

            TweetUserRole(float score) {
                  this.score = score;
            }

            public float ratio () {
                  return score;
            }
      }


      public enum CommenterType {
            KOL(5),
            NON_KOL(1);

            private final int score;

            CommenterType(int score) {
                  this.score = score;
            }

            public int score () {
                  return score;
            }
      }


      public enum NonKOLActivityType {
            QUOTE(4),
            REPOST(2),
            COMMENT(1);

            private final int score;

            NonKOLActivityType(int score) {
                  this.score = score;
            }

            public int score () {
                  return score;
            }
      }
}

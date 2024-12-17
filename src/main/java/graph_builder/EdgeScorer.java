package graph_builder;

public class EdgeScorer {
      public enum KOLActivityType {
            FOLLOW(2),
            TWEET(5),
            QUOTE(4),
            REPOST(1),
            COMMENT(1);

            private final int score;

            KOLActivityType (int score) {
                  this.score = score;
            }

            public int score () {
                  return score;
            }
      }

      public enum TweetRatio {
            AUTHOR(0.5f),
            COMMENT(0.5f);

            private final float score;

            TweetRatio (float score) {
                  this.score = score;
            }

            public float ratio () {
                  return score;
            }
      }

      public enum Comment {
            KOL(5),
            NON_KOL(1);

            private final int score;

            Comment (int score) {
                  this.score = score;
            }

            public int score () {
                  return score;
            }
      }

      public enum Non_KOL {
            QUOTE(4),
            REPOST(1),
            COMMENT(1);

            private final int score;

            Non_KOL (int score) {
                  this.score = score;
            }

            public int score () {
                  return score;
            }
      }
}

package graph_builder;

public class EdgeScore {
      public enum KOL {
            FOLLOW(1),
            TWEET(5),
            QUOTE(5),
            REPOST(2),
            COMMENT(1);

            private final int score;

            KOL (int score) {
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
            QUOTE(5),
            REPOST(2),
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

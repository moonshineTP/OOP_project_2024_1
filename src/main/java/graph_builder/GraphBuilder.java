package graph_builder;

import com.google.gson.*;
import data.constant.Constant;
import jsonIO.CustomJsonReader;
import jsonIO.CustomJsonWriter;

import java.util.*;

/**
 * This class use the user_data and tweet_data to create a json file describing a graph with
 * weight for the pagerank algorithm.
 */
public class GraphBuilder {
      public JsonObject user_data;
      public JsonObject tweet_data;
      JsonObject kol_data;
      JsonObject non_kol_data;

      String file_path;

      public GraphBuilder (String file_path) {
            this.file_path = file_path;

            user_data = CustomJsonReader.read(Constant.USER_DATA_FILE_PATH);
            tweet_data = CustomJsonReader.read(Constant.TWEET_DATA_FILE_PATH);
            kol_data = user_data.getAsJsonObject("KOL");
            non_kol_data = user_data.getAsJsonObject("Non-KOL");
      }

      public static void main () {
            String file_path = "C:\\Users\\admin\\IdeaProjects\\OOP_project\\untitled\\src\\main\\java\\graph_builder\\Graph_data.json";
            GraphBuilder graph_builder = new GraphBuilder(file_path);

            graph_builder.initialize();
            graph_builder.set_target();
            graph_builder.build_vertices();
            graph_builder.build_KOL_edges();
            graph_builder.build_Tweet_edges();
            graph_builder.build_non_KOL_edges();
      }

      public void initialize() {
            System.out.println("/// Initialize graph data");

            JsonObject graph_data = new JsonObject();
            graph_data.add("target", new JsonArray());
            graph_data.add("graph", new JsonObject());

            CustomJsonWriter.write(graph_data, file_path);
      }

      public void set_target() {
            System.out.println("/// Set target");

            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonArray target = graph_data.getAsJsonArray("target");

            for (String handle: kol_data.keySet()) {
                  target.add(handle);
            }

            CustomJsonWriter.write(graph_data, file_path);
      }

      public void build_vertices() {
            System.out.println("/// Building vertices");

            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");

            /// Initialize containers
            Map<String, Float> kol_scores = new HashMap<>();
            Map<String, Float> tweet_scores = new HashMap<>();
            Map<String, Float> non_kol_scores = new HashMap<>();

            /// Calculating scores
            VertexScorer vertex_scorer = new VertexScorer();

            float total_kol_score = 0, total_non_kol_score = 0, total_tweet_score = 0;
            for (String kol_handle: kol_data.keySet()) {
                  JsonObject kol = kol_data.getAsJsonObject(kol_handle);
                  float score = vertex_scorer.scoreKOL(kol);

                  kol_scores.put(kol_handle, score);
                  total_kol_score += score;
            }

            List<Map.Entry<String, Float>> kol_score_list = new ArrayList<>(kol_scores.entrySet());
            kol_score_list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
            int order = 1;
            for (Map.Entry<String, Float> entry: kol_score_list) {
                  System.out.println(order + ". " + entry.getKey() + ": " + entry.getValue());
                  order++;
            }

            for (String tweet_id: tweet_data.keySet()) {
                  JsonObject tweet = tweet_data.getAsJsonObject(tweet_id);
                  float score = vertex_scorer.scoreTweet(tweet);

                  tweet_scores.put(tweet_id, score);
                  total_tweet_score += score;
            }
            for (String non_kol_handle: non_kol_data.keySet()) {
                  float score = vertex_scorer.scoreNonKOL();

                  non_kol_scores.put(non_kol_handle, score);
                  total_non_kol_score += score;
            }
            // print result
            System.out.println("Total kol score: " + total_kol_score);
            System.out.println("Total tweet score: " + total_tweet_score);
            System.out.println("Total non-KOL score: " + total_non_kol_score);

            /// Calculate total score for normalization
            float total_score = total_kol_score + total_tweet_score + total_non_kol_score;

            /// Adding vertices to the graph
            for (String kol: kol_scores.keySet()) {
                  JsonObject vertex = new JsonObject();
                  vertex.addProperty("score", kol_scores.get(kol) / total_score);
                  graph.add(kol, vertex);
            }
            for (String id: tweet_scores.keySet()) {
                  JsonObject vertex = new JsonObject();
                  vertex.addProperty("score", tweet_scores.get(id) / total_score);
                  graph.add(id, vertex);
            }
            for (String non_kol: non_kol_scores.keySet()) {
                  JsonObject vertex = new JsonObject();
                  vertex.addProperty("score", non_kol_scores.get(non_kol) / total_score);
                  graph.add(non_kol, vertex);
            }

            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);
      }

      public void build_KOL_edges () {
            System.out.println("/// Building KOL edges");

            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");

            /// Loop
            int total_edge = 0;
            for (String kol_handle: kol_data.keySet()) {
                  /// Get data
                  JsonObject kol = kol_data.getAsJsonObject(kol_handle);

                  JsonArray following_list = kol.getAsJsonArray("following_kol_handle_list");
                  JsonArray tweet_list = new JsonArray();
                  for (String id: tweet_data.keySet()) {
                        if (kol_handle.equals(tweet_data.getAsJsonObject(id).get("author").getAsString())) {
                              tweet_list.add(id);
                        }
                  }
                  JsonArray quote_list = kol.getAsJsonArray("quote_tweet_id_list");
                  JsonArray repost_list = kol.getAsJsonArray("repost_tweet_id_list");
                  JsonArray comment_list = kol.getAsJsonArray("comment_tweet_id_list");


                  /// Calculate score and add edges
                  int total_score = EdgeScore.KOL.FOLLOW.score() * following_list.size()
                                    + EdgeScore.KOL.TWEET.score() * tweet_list.size()
                                    + EdgeScore.KOL.QUOTE.score() * quote_list.size()
                                    + EdgeScore.KOL.REPOST.score() * repost_list.size()
                                    + EdgeScore.KOL.COMMENT.score() * comment_list.size();

                  JsonObject edges = new JsonObject();

                  addMultipleByIncrement(edges, following_list, 1.0f * EdgeScore.KOL.FOLLOW.score() / total_score);
                  addMultipleByIncrement(edges, tweet_list, 1.0f * EdgeScore.KOL.TWEET.score() / total_score);
                  addMultipleByIncrement(edges, quote_list, 1.0f * EdgeScore.KOL.QUOTE.score() / total_score);
                  addMultipleByIncrement(edges, repost_list, 1.0f * EdgeScore.KOL.REPOST.score() / total_score);
                  addMultipleByIncrement(edges, comment_list, 1.0f * EdgeScore.KOL.COMMENT.score() / total_score);

                  /// Add edge list to the graph
                  JsonObject vertex = graph.getAsJsonObject(kol_handle);

                  vertex.add("edges", edges);

                  int edge_count = following_list.size() + tweet_list.size() + quote_list.size()
                        + repost_list.size() + comment_list.size();
                  total_edge += edge_count;
            }

            System.out.println("Total edges created: " + total_edge);

            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);
      }

      public void build_Tweet_edges () {
            System.out.println("/// Building tweet edges");

            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");

            /// Loop
            int total_edge = 0;
            for (String id : tweet_data.keySet()) {
                  /// Get data
                  List<String> kol_commenter_list = new LinkedList<>();
                  List<String> non_kol_commenter_list = new LinkedList<>();

                  // add kol commenters
                  for (String handle : kol_data.keySet()) {
                        JsonObject kol = kol_data.getAsJsonObject(handle);
                        JsonArray comment_id_list = kol.getAsJsonArray("comment_tweet_id_list");
                        if (comment_id_list.contains(new JsonPrimitive(id))) {
                              kol_commenter_list.add(handle);
                        }
                  }

                  // add non_kol commenters
                  for (String handle : non_kol_data.keySet()) {
                        JsonObject non_kol = non_kol_data.getAsJsonObject(handle);
                        JsonArray comment_id_list = non_kol.getAsJsonArray("comment_tweet_id_list");
                        if (comment_id_list.contains(new JsonPrimitive(id))) {
                              non_kol_commenter_list.add(handle);
                        }
                  }

                  /// Calculate score and make edges
                  JsonObject edges = new JsonObject();

                  // add author edge
                  String author = tweet_data.getAsJsonObject(id).get("author").getAsString();
                  edges.addProperty(author, EdgeScore.TweetRatio.AUTHOR.ratio());

                  // add commenter edge
                  int total_score = kol_commenter_list.size() + non_kol_commenter_list.size();

                  float kol_score = EdgeScore.TweetRatio.COMMENT.ratio()
                                          * EdgeScore.Comment.KOL.score()
                                                / total_score;
                  addMultipleByIncrement(edges, kol_commenter_list, kol_score);

                  float non_kol_score = EdgeScore.TweetRatio.COMMENT.ratio()
                                          * EdgeScore.Comment.NON_KOL.score()
                                                / total_score;
                  addMultipleByIncrement(edges, non_kol_commenter_list, non_kol_score);

                  /// Add edge list and edge count to the vertex
                  JsonObject vertex = graph.getAsJsonObject(id);

                  vertex.add("edges", edges);

                  int edge_count = 1 + kol_commenter_list.size() + non_kol_commenter_list.size();
                  total_edge += edge_count;
            }

            System.out.println("Total edges created: " + total_edge);

            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);
      }

      public void build_non_KOL_edges () {
            System.out.println("/// Building non-KOL edges");

            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");

            /// Loop
            int total_edge = 0;
            for (String non_kol_handle: non_kol_data.keySet()) {
                  /// Get data
                  JsonObject non_kol = non_kol_data.getAsJsonObject(non_kol_handle);

                  JsonArray quote_list = non_kol.getAsJsonArray("quote_tweet_id_list");
                  JsonArray repost_list = non_kol.getAsJsonArray("repost_tweet_id_list");
                  JsonArray comment_list = non_kol.getAsJsonArray("comment_tweet_id_list");


                  /// Calculate score and add edges
                  int total_score = EdgeScore.Non_KOL.QUOTE.score() * quote_list.size()
                                    + EdgeScore.Non_KOL.REPOST.score() * repost_list.size()
                                    + EdgeScore.Non_KOL.COMMENT.score() * comment_list.size();

                  JsonObject edges = new JsonObject();

                  addMultipleByIncrement(edges, quote_list, 1.0f * EdgeScore.Non_KOL.QUOTE.score() / total_score);
                  addMultipleByIncrement(edges, repost_list, 1.0f * EdgeScore.Non_KOL.REPOST.score() / total_score);
                  addMultipleByIncrement(edges, comment_list, 1.0f * EdgeScore.Non_KOL.COMMENT.score() / total_score);


                  /// Add edge list to the graph
                  JsonObject vertex = graph.getAsJsonObject(non_kol_handle);
                  vertex.add("edges", edges);

                  int edge_count = quote_list.size() + repost_list.size() + comment_list.size();
                  total_edge += edge_count;
            }

            System.out.println("Total edges created: " + total_edge);

            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);
      }

      private void addByIncrement (JsonObject json_object, String key, float val) {
            if (json_object.has(key)) {
                  float prev = json_object.get(key).getAsFloat();
                  json_object.addProperty(key, prev + val);
            }
            else json_object.addProperty(key, val);
      }

      private void addMultipleByIncrement (JsonObject json_obj, JsonArray json_arr, float score) {
            for (JsonElement el: json_arr) {
                  addByIncrement(json_obj, el.getAsString(), score);
            }
      }

      private void addMultipleByIncrement (JsonObject json_obj, List<String> arr, float score) {
            for (String str: arr) {
                  addByIncrement(json_obj, str, score);
            }
      }
}

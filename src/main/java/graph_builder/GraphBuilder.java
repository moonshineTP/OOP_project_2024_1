package graph_builder;

import com.google.gson.*;
import json.CustomJsonReader;
import json.CustomJsonWriter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class use the user_data and tweet_data to create a json file describing a graph with
 * weight for the pagerank algorithm.
 */
public class GraphBuilder {
      public JsonObject user_data;
      public JsonObject tweet_data;
      JsonObject kol_data;
      JsonObject non_kol_data;
      VertexScorer vertex_scorer;
      EdgeScorer edge_scorer;

      String file_path;

      public GraphBuilder (String file_path) {
            this.file_path = file_path;

            user_data = CustomJsonReader.read("untitled/data/User_data.json");
            tweet_data = CustomJsonReader.read("untitled/data/Tweet_data.json");
            kol_data = user_data.getAsJsonObject("KOL");
            non_kol_data = user_data.getAsJsonObject("Non-KOL");

            vertex_scorer = new VertexScorer();
            edge_scorer = new EdgeScorer();
      }


      public static void main () {
            String file_path = "untitled/data/Graph_data.json";
            GraphBuilder graph_builder = new GraphBuilder(file_path);

            graph_builder.initialize();               // initialize graph_data file
            graph_builder.setTargetAsKOL();           // set
            graph_builder.buildVertices();
            graph_builder.buildKOLEdges();
            graph_builder.buildTweetEdges();
            graph_builder.buildNonKOLEdges();
      }


      public void initialize() {
            System.out.println("/// Initialize graph data");

            JsonObject graph_data = new JsonObject();
            graph_data.add("target", new JsonArray());
            graph_data.add("graph", new JsonObject());

            CustomJsonWriter.write(graph_data, file_path);
      }


      public void setTarget (Set<String> name_set) {
            System.out.println("/// Set target");

            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonArray target = graph_data.getAsJsonArray("target");

            for (String name: name_set) {
                  target.add(name);
            }

            CustomJsonWriter.write(graph_data, file_path);
      }


      public void setTargetAsKOL () {
            setTarget(kol_data.keySet());
      }


      public void setTargetAsTweet () {
            setTarget(tweet_data.keySet());
      }


      public void buildVertices() {
            System.out.println("/// Building vertices");


            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");


            /// Initialize containers and variables
            Map<String, Float> kol_scores = new LinkedHashMap<>();
            Map<String, Float> tweet_scores = new LinkedHashMap<>();
            Map<String, Float> non_kol_scores = new LinkedHashMap<>();
            float total_kol_score = 0, total_non_kol_score = 0, total_tweet_score = 0;


            /// Calculate scores
            for (String kol_handle : kol_data.keySet()) {
                  float score = vertex_scorer.scoreKOL(kol_data.getAsJsonObject(kol_handle));
                  kol_scores.put(kol_handle, score);
                  total_kol_score += score;
            }

            for (String tweet_id : tweet_data.keySet()) {
                  float score = vertex_scorer.scoreTweet(tweet_data.getAsJsonObject(tweet_id));
                  tweet_scores.put(tweet_id, score);
                  total_tweet_score += score;
            }

            for (String non_kol_handle : non_kol_data.keySet()) {
                  float score = vertex_scorer.scoreNonKOL();
                  non_kol_scores.put(non_kol_handle, score);
                  total_non_kol_score += score;
            }


            /// Normalize scores and add vertices to the graph
            float total_score = total_kol_score + total_tweet_score + total_non_kol_score;
            Map<String, Float>[] scoreMaps = new Map[]{kol_scores, tweet_scores, non_kol_scores};

            for (Map<String, Float> scores : scoreMaps) {
                  for (Map.Entry<String, Float> entry : scores.entrySet()) {
                        JsonObject vertex = new JsonObject();
                        vertex.addProperty("score", entry.getValue() / total_score);
                        graph.add(entry.getKey(), vertex);
                  }
            }


            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);


            /// print result
            System.out.println("Total vertices: " + graph.size());

            System.out.println("Total KOLs: " + kol_scores.size());
            System.out.println("Total KOL score: " + total_kol_score);

            System.out.println("Total tweets: " + tweet_scores.size());
            System.out.println("Total tweet score: " + total_tweet_score);

            System.out.println("Total non-KOLs: " + non_kol_scores.size());
            System.out.println("Total non-KOL score: " + total_non_kol_score);
      }


      public void buildKOLEdges() {
            System.out.println("/// Building KOL edges");


            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");


            /// Loop through KOL handle
            int total_edge = 0;
            for (String kol_handle: kol_data.keySet()) {
                  JsonObject kol = kol_data.getAsJsonObject(kol_handle);


                  /// Collect data lists
                  JsonArray following_list = kol.getAsJsonArray("following_kol_handle_list");

                  JsonArray quote_list = kol.getAsJsonArray("quote_tweet_id_list");
                  JsonArray repost_list = kol.getAsJsonArray("repost_tweet_id_list");
                  JsonArray comment_list = kol.getAsJsonArray("comment_tweet_id_list");

                  // Collect tweet list (optimized with parallelStream())
                  JsonArray tweet_list = tweet_data.entrySet().parallelStream()
                        .filter(entry -> kol_handle.equals(entry.getValue().getAsJsonObject().get("author").getAsString()))
                        .map(Map.Entry::getKey)
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);


                  /// Calculate score and add edges
                  Map<EdgeScorer.KOLActivityType, Integer> edge_type_count = new LinkedHashMap<>();
                  edge_type_count.put(EdgeScorer.KOLActivityType.FOLLOW, following_list.size());
                  edge_type_count.put(EdgeScorer.KOLActivityType.TWEET, tweet_list.size());
                  edge_type_count.put(EdgeScorer.KOLActivityType.QUOTE, quote_list.size());
                  edge_type_count.put(EdgeScorer.KOLActivityType.REPOST, repost_list.size());
                  edge_type_count.put(EdgeScorer.KOLActivityType.COMMENT, comment_list.size());

                  float total_score = 0;
                  for (Map.Entry<EdgeScorer.KOLActivityType, Integer> entry: edge_type_count.entrySet()) {
                        total_score += edge_scorer.scoreKOLEdge(entry.getKey()) * entry.getValue();
                  }

                  /// Add them to edges
                  JsonObject edges = new JsonObject();
                  for (EdgeScorer.KOLActivityType type: edge_type_count.keySet()) {
                        JsonArray list = switch (type) {
                              case FOLLOW -> following_list;
                              case TWEET -> tweet_list;
                              case QUOTE -> quote_list;
                              case REPOST -> repost_list;
                              case COMMENT -> comment_list;
                        };

                        // add edges with normalized score
                        addMultipleByIncrement(edges, list, edge_scorer.scoreKOLEdge(type) / total_score);
                  }


                  /// Add edge list to the graph
                  JsonObject vertex = graph.getAsJsonObject(kol_handle);
                  vertex.add("edges", edges);

                  int edge_count = edge_type_count.values().stream().mapToInt(Integer::intValue).sum();
                  total_edge += edge_count;
            }

            /// Print statistic
            System.out.println("Total edges created: " + total_edge);

            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);
      }

      public void buildTweetEdges() {
            System.out.println("/// Building tweet edges");


            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");


            /// Loop
            int total_edge = 0;
            for (String id : tweet_data.keySet()) {
                  /// Get data
                  List<String> kol_commenter_list = kol_data.entrySet().parallelStream()
                        .filter(entry -> entry.getValue().getAsJsonObject().getAsJsonArray("comment_tweet_id_list")
                              .contains(new JsonPrimitive(id)))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                  List<String> non_kol_commenter_list = non_kol_data.entrySet().parallelStream()
                        .filter(entry -> entry.getValue().getAsJsonObject().getAsJsonArray("comment_tweet_id_list")
                              .contains(new JsonPrimitive(id)))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());


                  /// Calculate score and add to edges
                  JsonObject edges = new JsonObject();

                  /// Add edge representing authorship
                  String author = tweet_data.getAsJsonObject(id).get("author").getAsString();
                  edges.addProperty(author, edge_scorer.scoreTweetAuthorEdge());

                  /// Add edge representing comments
                  // raw score
                  float raw_kol_score = edge_scorer.scoreTweetCommenterEdge(EdgeScorer.CommenterType.KOL);
                  float raw_non_kol_score = edge_scorer.scoreTweetCommenterEdge(EdgeScorer.CommenterType.NON_KOL);
                  float total_score = raw_kol_score * kol_commenter_list.size() + raw_non_kol_score * non_kol_commenter_list.size();
                  
                  // normalized score
                  float kol_score = EdgeScorer.TweetUserRole.COMMENT.ratio() * raw_non_kol_score / total_score;
                  float non_kol_score = EdgeScorer.TweetUserRole.COMMENT.ratio() * raw_non_kol_score / total_score;

                  // add edges by its score
                  for (String handle: kol_commenter_list) {
                        addMultipleByIncrement(edges, kol_commenter_list, kol_score);
                  }
                  for (String handle : non_kol_commenter_list) {
                      addMultipleByIncrement(edges, non_kol_commenter_list, non_kol_score);
                  }


                  /// Add edges to vertex
                  JsonObject vertex = graph.getAsJsonObject(id);
                  vertex.add("edges", edges);

                  int edge_count = 1 + kol_commenter_list.size() + non_kol_commenter_list.size();
                  total_edge += edge_count;
            }

            /// Print statistic
            System.out.println("Total edges created: " + total_edge);

            /// Write the graph
            CustomJsonWriter.write(graph_data, file_path);
      }

      public void buildNonKOLEdges() {
            System.out.println("/// Building non-KOL edges");


            /// Get graph from file path
            JsonObject graph_data = CustomJsonReader.read(file_path);
            JsonObject graph = graph_data.getAsJsonObject("graph");


            /// Loop
            int total_edge = 0;
            for (String non_kol_handle: non_kol_data.keySet()) {
                  JsonObject non_kol = non_kol_data.getAsJsonObject(non_kol_handle);

                  /// Get data lists
                  JsonArray quote_list = non_kol.getAsJsonArray("quote_tweet_id_list");
                  JsonArray repost_list = non_kol.getAsJsonArray("repost_tweet_id_list");
                  JsonArray comment_list = non_kol.getAsJsonArray("comment_tweet_id_list");


                  /// Calculate score and add edges
                  Map<EdgeScorer.NonKOLActivityType, Integer> edge_type_count = new LinkedHashMap<>();
                  edge_type_count.put(EdgeScorer.NonKOLActivityType.QUOTE, quote_list.size());
                  edge_type_count.put(EdgeScorer.NonKOLActivityType.REPOST, repost_list.size());
                  edge_type_count.put(EdgeScorer.NonKOLActivityType.COMMENT, comment_list.size());

                  float total_score = 0;
                  for (Map.Entry<EdgeScorer.NonKOLActivityType, Integer> entry: edge_type_count.entrySet()) {
                        total_score += edge_scorer.scoreNonKOLEdge(entry.getKey()) * entry.getValue();
                  }

                  /// Form edge and add that to a container
                  JsonObject edges = new JsonObject();

                  for (EdgeScorer.NonKOLActivityType type: edge_type_count.keySet()) {
                        JsonArray list = switch (type) {
                              case QUOTE -> quote_list;
                              case REPOST -> repost_list;
                              case COMMENT -> comment_list;
                        };

                        // add edges with normalized score
                        addMultipleByIncrement(edges, list, edge_scorer.scoreNonKOLEdge(type) / total_score);
                  }


                  /// Add edge list to the graph
                  JsonObject vertex = graph.getAsJsonObject(non_kol_handle);
                  vertex.add("edges", edges);

                  int edge_count = quote_list.size() + repost_list.size() + comment_list.size();
                  total_edge += edge_count;
            }

            /// Print statistics
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

package pagerank;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jsonIO.CustomJsonReader;
import org.openqa.selenium.json.TypeToken;

import java.util.*;


public class PageRank {
      public List<String> target;
      public Graph graph;

      public PageRank (JsonObject graph_data) {
            JsonArray target_jsonArray = graph_data.getAsJsonArray("target");
            JsonObject graph_jsonObject = graph_data.getAsJsonObject("graph");

            this.target = (new Gson()).fromJson(target_jsonArray,
                              new TypeToken<ArrayList<String>>() {}.getType());
            this.graph = Graph.extractGraphFromJson(graph_jsonObject);
      }

      public static void main () {
            String file_path = "C:\\Users\\admin\\IdeaProjects\\OOP_project\\untitled\\src\\main\\java\\graph_builder\\Graph_data.json";
            JsonObject graph_data = CustomJsonReader.read(file_path);

            PageRank pagerank = new PageRank(graph_data);

            // Params for pagerank
            long iter = 20000000;
            float damping_factor = 0.85f;

            // pagerank
            long start_time = System.currentTimeMillis();
            Map<String, Double> score_map = pagerank.compute(iter, damping_factor);
            long end_time = System.currentTimeMillis();

            // sort the result in descending order of score
            List<Map.Entry<String, Double>> score_list = new ArrayList<>(score_map.entrySet());
            score_list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            // print result
            int order = 1;
            for (Map.Entry<String, Double> entry: score_list) {
                  System.out.println(order + ". " + entry.getKey() + ": " + (100 * entry.getValue()));
                  order++;
            }

            // print total time taken
            System.out.println();
            System.out.println("Total time taken: " + 1.0f * (end_time - start_time) / 1000 + "s");
      }

      public Map<String, Double> compute (long iter, float d) {
            System.out.println("/// Start computing ///");

            /// Initialize result
            Map<String, Long> visit_counter = new LinkedHashMap<>();
            int total_visit = 0;

            for (String target_id: target) {
                  visit_counter.put(target_id, 0L);
            }

            /// Initialize random element
            Random random = new Random();

            Vertex vertex = graph.getVertex(random.nextFloat());
            for (int i = 0; i < iter; i++) {
                  String id = vertex.id;

                  if (visit_counter.containsKey(id)) {
                        long visit_count = visit_counter.get(id);
                        visit_counter.replace(id, visit_count + 1);
                        total_visit++;
                  }

                  /// Transition
                  float vertex_seed = random.nextFloat();
                  // if the vertex has no out edge, teleport
                  if (vertex.out_degree == 0) {
                        vertex = graph.getVertex(vertex_seed);
                        continue;
                  }

                  // if not teleport to a random vertex with probability 1 - d
                  if (random.nextFloat() > d) {
                        vertex = graph.getVertex(vertex_seed);
                        continue;
                  }

                  // else, choose a random edge (distributed by score) to travel to the next vertex
                  float edge_seed = random.nextFloat();
                  Edge choosen_edge = graph.getEdge(id, edge_seed);
                  vertex = choosen_edge.target_vertex;
            }

            if (total_visit == 0) return null;

            /// Return result
            Map<String, Double> result = new LinkedHashMap<>();
            for (Map.Entry<String, Long> entry: visit_counter.entrySet()) {
                  result.put(entry.getKey(), 1.0d * entry.getValue() / total_visit);
            }

            System.out.println("/// Computing done");
            return result;
      }
}

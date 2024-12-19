package pagerank.graph_model;

import com.google.gson.JsonObject;

import java.util.*;

public class Graph {
      public Map<String, Vertex> vertices;
      public TreeMap<Float, Vertex> vertices_distribution;
      public Map<String, TreeMap<Float, Edge>> adjacency_map;



      /// Constructor
      public Graph () {
            vertices = new LinkedHashMap<>();
            adjacency_map = new LinkedHashMap<>();
            vertices_distribution = new TreeMap<>();
      }

      /// Method
      public static Graph extractGraphFromJson (JsonObject graph_jsonObject) {
            Graph graph = new Graph();

            /// Add vertex_set
            float vertex_sum = 0;
            for (String id: graph_jsonObject.keySet()) {
                  JsonObject vertex_jsonObject = graph_jsonObject.getAsJsonObject(id);

                  float score = vertex_jsonObject.get("score").getAsFloat();
                  int edge_count = ((JsonObject) vertex_jsonObject.get("edges")).size();

                  /// Create vertex
                  Vertex vertex = new Vertex(id, score, edge_count);

                  /// Add elements to graph
                  graph.vertices.put(id, vertex);

                  graph.vertices_distribution.put(vertex_sum, vertex);
                  vertex_sum += vertex.score;
            }

            /// Add adjacency map
            for (String id: graph.vertices.keySet()) {
                  JsonObject edges_jsonObject = graph_jsonObject.getAsJsonObject(id).getAsJsonObject("edges");

                  TreeMap<Float, Edge> edges = new TreeMap<>();
                  float edge_sum = 0;
                  for (String neighbor_id: edges_jsonObject.keySet()) {
                        Vertex neighbor_vertex = graph.vertices.get(neighbor_id);
                        float score = edges_jsonObject.get(neighbor_id).getAsFloat();

                        Edge edge = new Edge(neighbor_vertex, score);
                        edges.put(edge_sum, edge);
                        edge_sum += edge.score;
                  }

                  graph.adjacency_map.put(id, edges);
            }

            return graph;
      }

      public Vertex getVertex (float param) {
            return vertices_distribution.get(vertices_distribution.floorKey(param));
      }

      public Edge getEdge (String id, float param) {
            TreeMap<Float, Edge> edges = adjacency_map.get(id);
            return edges.get(edges.floorKey(param));
      }
}

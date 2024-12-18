package pagerank.graph_element;

public class Edge {
      public Vertex target_vertex;
      public float score;

      public Edge (Vertex vertex, float score) {
            this.target_vertex = vertex;
            this.score= score;
      }
}

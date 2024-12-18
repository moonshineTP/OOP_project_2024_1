package pagerank.graph_element;

public class Vertex {
      public String id;
      public float score;
      public int out_degree;

      public Vertex (String id, float score, int out_degree) {
            this.id = id;
            this.score = score;
            this.out_degree = out_degree;
      }

      public boolean equals(Vertex vertex) {
            return this.id.equals(vertex.id);
      }
}

package pagerank;

import com.google.gson.JsonObject;
import json.CustomJsonReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PageRankWithErrorLimit {
      private static final long INITIAL_ITERATION = 10000000;
      private PageRank pagerank;

      public PageRankWithErrorLimit (PageRank pagerank) {
            this.pagerank = pagerank;
      }

      private Map<String, Double> rank (double error_limit, float damping_factor, boolean is_parallel) {
            long iteration = INITIAL_ITERATION;
            Map<String, Double> rank;
            while (true) {
                  System.out.println("Current iteration used: " + iteration);

                  // Compute two pagerank list
                  rank = pagerank.compute(iteration, damping_factor, true, is_parallel);
                  Map<String, Double> rank_2 = pagerank.compute(iteration, damping_factor, true, is_parallel);


                  // Find the maximum error between them
                  double max_error = 0;
                  for (String id: rank.keySet()) {
                        double diff = Math.abs(rank.get(id) - rank_2.get(id));
                        max_error = Math.max(max_error, diff);
                  }

                  // Print error margin
                  System.out.println("Maximum error: " + max_error);


                  // If error is larger than limit, double the iteration and run again
                  if (max_error > error_limit) {
                        System.out.println("Result doesn't diverge. Increase iterations\n");
                        iteration *= 2;
                  }
                  // Else, break
                  else {
                        System.out.println("Result diverges. Stop checking\n");
                        break;
                  }
            }

            System.out.println("Recommended iteration :" + iteration);
            return rank;
      }

      public static void main () {
            /// Initialize pagerank
            String file_path = "./data/Graph_data.json";
            JsonObject graph_data = CustomJsonReader.read(file_path);
            PageRank pagerank = new PageRank(graph_data);
            PageRankWithErrorLimit ranker = new PageRankWithErrorLimit(pagerank);


            /// Compute
            double acceptable_error_margin = 0.0001d;

            long start_time = System.currentTimeMillis();
            Map<String, Double> score_map = ranker.rank(acceptable_error_margin, 0.85f, true);
            long end_time = System.currentTimeMillis();

            System.out.println("Total time taken: " + 1.0f * (end_time - start_time) / 1000 + "s");


            /// Data cleaning
            // sort the result in descending order of score
            List<Map.Entry<String, Double>> score_list = new ArrayList<>(score_map.entrySet());
            score_list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            // print result
            int order = 1;
            for (Map.Entry<String, Double> entry: score_list) {
                  System.out.println(order + ". " + entry.getKey() + ": " + (100 * entry.getValue()));
                  order++;
            }
      }
}

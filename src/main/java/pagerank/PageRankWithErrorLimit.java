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

      private Map<String, Double> rank (double error_limit, float damping_factor) {
            long iteration = INITIAL_ITERATION;
            Map<String, Double> rank;
            while (true) {
                  rank = pagerank.compute(iteration, damping_factor);

                  Map<String, Double> rank_2 = pagerank.compute(iteration, damping_factor);

                  double max_error = 0;
                  for (String id: rank.keySet()) {
                        double diff = Math.abs(rank.get(id) - rank_2.get(id));
                        max_error = Math.max(max_error, diff);
                  }

                  System.out.println("Maximum error: " + max_error);

                  if (max_error > error_limit) {
                        System.out.println("Result doesn't diverge. Increase iterations\n");
                        iteration *= 2;
                  }
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
            String file_path = "C:\\Users\\admin\\IdeaProjects\\OOP_project\\untitled\\data\\Graph_data.json";
            JsonObject graph_data = CustomJsonReader.read(file_path);
            PageRank pagerank = new PageRank(graph_data);
            PageRankWithErrorLimit ranker = new PageRankWithErrorLimit(pagerank);


            /// Compute
            double acceptable_error_margin = 0.0002d;

            long start_time = System.currentTimeMillis();
            Map<String, Double> score_map = ranker.rank(acceptable_error_margin, 0.85f);
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

package io.confluent.developer.simple;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * A Flink job that counts the occurrences of words in a stream of text.
 * This is a classic example of a stateful streaming application.
 */
public class WordCountJob {

  public static void main(String[] args) throws Exception {
    // Set up the execution environment
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create a DataStream from some sentences
    DataStream<String> inputStream = env.fromData(
        "apple banana cherry",
        "banana date elderberry",
        "cherry apple date",
        "date elderberry apple",
        "elderberry cherry banana"
    );

    // Perform a transformation - split sentences into words and count them
    DataStream<Tuple2<String, Integer>> resultStream = inputStream
        // 1. Use flatMap to split each sentence into individual words
        //    and emit a tuple (word, 1) for each word
        .flatMap((String line, org.apache.flink.util.Collector<Tuple2<String, Integer>> out) -> {
          // Convert to lowercase and split the line into words using whitespace as delimiter
          String[] words = line.toLowerCase().split("\\s+");
          // For each word, emit a tuple with the word and count 1
          for (String word : words) {
            out.collect(new Tuple2<>(word, 1));
          }
        })
        // Explicitly specify the return type as Flink cannot infer it from lambda expressions
        .returns(Types.TUPLE(Types.STRING, Types.INT))
        // 2. Group the tuples by word (the first field, f0, in the tuple)
        .keyBy(value -> value.f0)
        // 3. Sum the counts (the second field, f1, in the tuple) for each word
        //    This creates a running sum as new elements arrive
        .sum(1);

    // Print the results to the console
    resultStream.print();

    // Execute the Flink job
    env.execute("Word Count Job");
  }
}

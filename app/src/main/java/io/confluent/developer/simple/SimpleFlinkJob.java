package io.confluent.developer.simple;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SimpleFlinkJob {

  public static void main(String[] args) throws Exception {
    // Set up the execution environment
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create a DataStream from some elements
    DataStream<String> inputStream = env.fromData("apple", "banana", "cherry", "date", "elderberry");

    // Perform a transformation
    DataStream<Tuple2<String, Integer>> resultStream = inputStream
        .map(value -> new Tuple2<>(value, value.length()))
        .returns(Types.TUPLE(Types.STRING, Types.INT));

    // Print the results to the console
    resultStream.print();

    // Execute the Flink job
    env.execute("Simple Flink Job");
  }
}
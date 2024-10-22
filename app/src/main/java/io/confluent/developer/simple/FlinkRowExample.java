package io.confluent.developer.simple;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

public class FlinkRowExample {

  public static void main(String[] args) throws Exception {
    // Set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create a DataStream of Row objects
    DataStream<Row> dataStream = env.fromData(
        Row.of("Alice", 28, "New York"),
        Row.of("Bob", 32, "San Francisco"),
        Row.of("Charlie", 25, "Los Angeles")
    );

    // Process the DataStream
    DataStream<Row> processedStream = dataStream
        .map((MapFunction<Row, Row>) value -> {
          String name = value.getField(0).toString();
          int age = (int) value.getField(1);
          String city = value.getField(2).toString();

          // Add a new field: uppercase name
          return Row.of(name, age, city, name.toUpperCase());
        }).returns(Types.ROW(Types.STRING, Types.INT, Types.STRING, Types.STRING));

    // Print the result
    processedStream.print();

    // Execute the Flink job
    env.execute("Flink Row Example");
  }
}
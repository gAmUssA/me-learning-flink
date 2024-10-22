package io.confluent.developer.simple;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;


public class FlinkRow2Example {

  public static void main(String[] args) throws Exception {
    // Set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create a DataStream from some elements
    DataStream<String> inputStream = env.fromData("apple", "banana", "cherry", "date", "elderberry");

    // Perform a transformation
    DataStream<Row> resultStream = inputStream
        .map(value -> Row.of(value, value.length()))
        .returns(Types.ROW(Types.STRING, Types.INT)); // без этого взрывается на этапе десериализации с помощью reflection

    // Print the result
    resultStream.print();

    // Execute the Flink job
    env.execute("Flink Row Example");
  }
}
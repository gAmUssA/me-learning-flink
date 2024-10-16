package io.confluent.developer.simple;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.junit5.MiniClusterExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import io.confluent.developer.support.CollectSink;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleFlinkJobTest {

  @RegisterExtension
  public static final MiniClusterExtension MINI_CLUSTER_RESOURCE = new MiniClusterExtension();

  @Test
  public void testSimpleFlinkJob() throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create a DataStream from some elements
    DataStream<String> inputStream = env.fromData("apple", "banana", "cherry", "date", "elderberry");

    // Perform the transformation with explicit type information
    DataStream<Tuple2<String, Integer>> resultStream = inputStream
        .map(value -> new Tuple2<>(value, value.length()))
        .returns(Types.TUPLE(Types.STRING, Types.INT));

    // Add a custom sink to collect the results
    resultStream.sinkTo(new CollectSink());

    resultStream.print();
    // Execute the job
    JobExecutionResult result = env.execute();

    // Verify the results
    List<Tuple2<String, Integer>> expected = List.of(
        new Tuple2<>("apple", 5),
        new Tuple2<>("banana", 6),
        new Tuple2<>("cherry", 6),
        new Tuple2<>("date", 4),
        new Tuple2<>("elderberry", 10)
    );

    // Use AssertJ to verify results, ignoring order
    assertThat(CollectSink.getCollectedElements())
        .hasSameElementsAs(expected)
        .hasSize(expected.size());
  }

}
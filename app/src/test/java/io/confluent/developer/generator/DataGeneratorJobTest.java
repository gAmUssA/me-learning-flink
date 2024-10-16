package io.confluent.developer.generator;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.junit5.MiniClusterExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.confluent.developer.support.CollectSink;

import static org.apache.flink.api.common.eventtime.WatermarkStrategy.noWatermarks;
import static org.assertj.core.api.Assertions.assertThat;

class DataGeneratorJobTest {

  @RegisterExtension
  public static final MiniClusterExtension MINI_CLUSTER_RESOURCE = new MiniClusterExtension();

  @Test
  public void testSimpleFlinkJob() throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    final DataGeneratorSource<DataGenerator.SunsetAirFlightData> dataGeneratorSource =
        new DataGeneratorSource<>(
            index -> DataGenerator.generateSunsetAirFlightData(),
            10L,
            RateLimiterStrategy.perSecond(10),
            Types.POJO(DataGenerator.SunsetAirFlightData.class)
        );
    final DataStreamSource<DataGenerator.SunsetAirFlightData> sunsetStream = env.fromSource(dataGeneratorSource, noWatermarks(), "sunset_source");
    sunsetStream.sinkTo(new CollectSink<>());

    sunsetStream.print();
    env.execute();

    assertThat(CollectSink.getCollectedElements())
        .hasSize(10);

  }

}
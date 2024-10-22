package io.confluent.developer.movies;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

import io.confluent.developer.movies.config.JacksonConfig;
import io.confluent.developer.movies.domain.Movie;
import io.confluent.developer.movies.domain.Rating;
import io.confluent.developer.movies.generator.FlinkDataGenerators;

public class MovieStreamingJob {

  private static final Logger LOG = LoggerFactory.getLogger(MovieStreamingJob.class);

  public static void main(String[] args) throws Exception {
    // Set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Configure ExecutionConfig for generic types
    ExecutionConfig config = env.getConfig();
    config.enableGenericTypes();

    // Register POJO classes
    config.registerPojoType(Movie.class);
    config.registerPojoType(Rating.class);
    
    Properties producerConfig = new Properties();
    try (InputStream stream = MovieStreamingJob.class.getClassLoader().getResourceAsStream("producer.properties")) {
      producerConfig.load(stream);
    }

    // Create generators with classpath resource
    FlinkDataGenerators generators = new FlinkDataGenerators("movies.jsonl");
    LOG.info("Initialized generators with {} movies", generators.getMovieCount());

    //  Create movie stream
    DataStream<Movie> movieStream = env
        .fromSource(
            generators.createMovieSource(),
            WatermarkStrategy.noWatermarks(),
            "Movie Source"
        );

    final KafkaRecordSerializationSchema<Movie> movieRecordSchema = KafkaRecordSerializationSchema.<Movie>builder()
        .setTopic("movies")
        .setValueSerializationSchema(new JsonSerializationSchema<>(JacksonConfig::createObjectMapper))
        .build();

    final KafkaSink<Movie> movieKafkaSink = KafkaSink.<Movie>builder()
        .setKafkaProducerConfig(producerConfig)
        .setRecordSerializer(movieRecordSchema)
        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .build();

    movieStream.sinkTo(movieKafkaSink);

    // Create rating stream (1 million ratings at 100 per second)
    DataStream<Rating> ratingStream = env
        .fromSource(
            generators.createRatingSource(1_000_000L, 100),
            WatermarkStrategy.noWatermarks(),
            "Rating Source"
        );

    // Add some basic processing
//    movieStream
//        .map(movie -> String.format("Movie loaded: %s (ID: %d)", movie.getTitle(), movie.getMovieId()))
//        .print();

//    ratingStream
//        .map(rating -> String.format("Rating received: movieId=%d, rating=%.1f",
//                                     rating.getMovieId(), rating.getRating()))
//        .print();

    KafkaRecordSerializationSchema<Rating> ratingSerializer = KafkaRecordSerializationSchema.<Rating>builder()
        .setTopic("ratings")
        .setValueSerializationSchema(new JsonSerializationSchema<>(JacksonConfig::createObjectMapper))
        .build();
    KafkaSink<Rating> ratingKafkaSink = KafkaSink.<Rating>builder()
        .setKafkaProducerConfig(producerConfig)
        .setRecordSerializer(ratingSerializer)
        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build();

    ratingStream
        .sinkTo(ratingKafkaSink)
        .name("rating_sink");
    // Execute program
    env.execute("Movie Rating Generator");
  }
}
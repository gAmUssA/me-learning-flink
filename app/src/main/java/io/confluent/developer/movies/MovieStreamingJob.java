package io.confluent.developer.movies;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import io.confluent.developer.movies.config.JacksonConfig;
import io.confluent.developer.movies.domain.Movie;
import io.confluent.developer.movies.domain.RatedMovie;
import io.confluent.developer.movies.domain.Rating;
import io.confluent.developer.movies.domain.RatingAverage;
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
//    config.registerPojoType(Movie.class);
//    config.registerPojoType(Rating.class);

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
    movieStream
        .map(movie -> String.format("Movie loaded: %s (ID: %d)", movie.getTitle(), movie.getMovieId()))
        .print();


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

    // Implement join between movies and ratings
    // Define key selectors for join
    KeySelector<Movie, Long> movieKeySelector = Movie::getMovieId;
    KeySelector<Rating, Long> ratingKeySelector = Rating::getMovieId;

    // Define accumulator class for calculating average rating
    class RatingAccumulator implements Serializable {
        private static final long serialVersionUID = 1L;
        double sum = 0.0;
        int count = 0;
    }

    // Define a rating aggregator to calculate average rating
    class RatingAggregateFunction implements AggregateFunction<Rating, RatingAccumulator, Double> {
        @Override
        public RatingAccumulator createAccumulator() {
            return new RatingAccumulator();
        }

        @Override
        public RatingAccumulator add(Rating rating, RatingAccumulator accumulator) {
            accumulator.sum += rating.getRating();
            accumulator.count++;
            return accumulator;
        }

        @Override
        public Double getResult(RatingAccumulator accumulator) {
            return accumulator.count > 0 ? accumulator.sum / accumulator.count : 0.0;
        }

        @Override
        public RatingAccumulator merge(RatingAccumulator a, RatingAccumulator b) {
            a.sum += b.sum;
            a.count += b.count;
            return a;
        }
    }

    // Process window function to create RatingAverage objects
    class RatingAverageProcessFunction 
            extends ProcessWindowFunction<Double, RatingAverage, Long, TimeWindow> {
        @Override
        public void process(Long movieId, 
                           Context context, 
                           Iterable<Double> averages, 
                           Collector<RatingAverage> out) {
            // There should be only one average per window
            Double avg = averages.iterator().next();
            out.collect(new RatingAverage(movieId, avg));
        }
    }

    // Aggregate ratings by movieId to get average rating
    DataStream<RatingAverage> averageRatings = ratingStream
        .keyBy(Rating::getMovieId)
        .window(TumblingProcessingTimeWindows.of(Time.seconds(30))) // Increased window size to 30 seconds
        .aggregate(new RatingAggregateFunction(), new RatingAverageProcessFunction());

    // Log average ratings for debugging
    averageRatings = averageRatings
        .map(avg -> {
            LOG.info("Average rating calculated: movieId={}, rating={}", avg.getMovieId(), avg.getAverageRating());
            return avg;
        });

    // Log movie stream for debugging
    movieStream = movieStream
        .map(movie -> {
            LOG.info("Movie received: id={}, title={}", movie.getMovieId(), movie.getTitle());
            return movie;
        });

    // Join movies with average ratings
    DataStream<RatedMovie> ratedMovies = movieStream
        .keyBy(Movie::getMovieId)
        .join(averageRatings)
        .where(Movie::getMovieId)
        .equalTo(RatingAverage::getMovieId)
        .window(TumblingProcessingTimeWindows.of(Time.seconds(30))) // Increased window size to 30 seconds
        .apply((JoinFunction<Movie, RatingAverage, RatedMovie>) 
            (movie, ratingAvg) -> {
                LOG.info("Joining movie id={} with rating={}", movie.getMovieId(), ratingAvg.getAverageRating());
                return RatedMovie.fromMovie(movie, ratingAvg.getAverageRating());
            });

    // Print some rated movies for debugging
    ratedMovies
        .map(ratedMovie -> String.format("Rated Movie: %s (ID: %d), Average Rating: %.2f", 
            ratedMovie.getTitle(), ratedMovie.getMovieId(), ratedMovie.getAverageRating()))
        .print();

    // Create Kafka sink for rated movies
    // Add custom serialization to ensure proper handling of RatedMovie objects
    JsonSerializationSchema<RatedMovie> jsonSerializer = new JsonSerializationSchema<>(JacksonConfig::createObjectMapper);

    KafkaRecordSerializationSchema<RatedMovie> ratedMovieSerializer = KafkaRecordSerializationSchema.<RatedMovie>builder()
        .setTopic("rated-movies")
        .setValueSerializationSchema(jsonSerializer)
        .build();

    // Log the serialized data for debugging
    ratedMovies = ratedMovies.map(ratedMovie -> {
        try {
            byte[] serialized = jsonSerializer.serialize(ratedMovie);
            LOG.info("Serialized RatedMovie: {}", new String(serialized));
        } catch (Exception e) {
            LOG.error("Error serializing RatedMovie: {}", e.getMessage(), e);
        }
        return ratedMovie;
    });

    KafkaSink<RatedMovie> ratedMovieKafkaSink = KafkaSink.<RatedMovie>builder()
        .setKafkaProducerConfig(producerConfig)
        .setRecordSerializer(ratedMovieSerializer)
        .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .build();

    ratedMovies
        .sinkTo(ratedMovieKafkaSink)
        .name("rated_movie_sink");

    // Execute program
    env.execute("Movie Rating Generator");
  }
}

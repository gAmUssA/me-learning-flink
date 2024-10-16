package io.confluent.developer.simple;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkJavaRecordExample {

  // Define a record to represent a person
  public record Person(String name, int age, String city) {

  }

  public static void main(String[] args) throws Exception {
    // Set up the streaming execution environment
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // Create a DataStream of Person records
    DataStream<Person> personStream = env.fromData(
        new Person("Alice", 28, "New York"),
        new Person("Bob", 32, "San Francisco"),
        new Person("Charlie", 25, "Los Angeles")
    );

    // Process the DataStream
    DataStream<String> processedStream = personStream
        .map(person -> person.name().toUpperCase() + " is " + person.age() + " years old and lives in " + person.city());

    // Print the result
    processedStream.print();

    // Execute the Flink job
    env.execute("Flink Java Record Example");
  }
}
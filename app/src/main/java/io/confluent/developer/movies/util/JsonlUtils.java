package io.confluent.developer.movies.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class JsonlUtils {

  public static <T> Stream<T> readJsonlFile(Path path, Class<T> targetClass, ObjectMapper objectMapper) throws IOException {
    return Files.lines(path)
        .map(line -> {
          try {
            return objectMapper.readValue(line, targetClass);
          } catch (IOException e) {
            throw new RuntimeException("Failed to parse line: " + line, e);
          }
        });
  }

  public static <T> void writeJsonlFile(Path path, List<T> objects, ObjectMapper objectMapper) throws IOException {
    List<String> lines = objects.stream()
        .map(obj -> {
          try {
            return objectMapper.writeValueAsString(obj);
          } catch (IOException e) {
            throw new RuntimeException("Failed to serialize object: " + obj, e);
          }
        })
        .toList();

    Files.write(path, lines);
  }
}
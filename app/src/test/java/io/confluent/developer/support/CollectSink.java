package io.confluent.developer.support;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectSink<T> implements Sink<T> {

  private static final List<Object> COLLECTED = new CopyOnWriteArrayList<>();

  @SuppressWarnings("deprecation")
  public SinkWriter<T> createWriter(final InitContext context) {
    return null;
  }

  @Override
  public SinkWriter<T> createWriter(WriterInitContext context) {
    return new SinkWriter<>() {
      @Override
      public void write(T element, Context context) {
        COLLECTED.add(element);
      }

      @Override
      public void flush(boolean endOfInput) {
        // No-op for this simple implementation
      }

      @Override
      public void close() {
        // No-op for this simple implementation
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> collectElements() {
    List<?> copiedList = List.copyOf(COLLECTED);
    return (List<T>) copiedList;
  }
}
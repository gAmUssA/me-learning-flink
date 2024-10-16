package io.confluent.developer.support;

import org.apache.flink.api.connector.sink2.Sink;
import org.apache.flink.api.connector.sink2.SinkWriter;
import org.apache.flink.api.connector.sink2.WriterInitContext;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectSink<T> implements Sink<T> {

  private static final List<Object> COLLECTED = new CopyOnWriteArrayList<>();
  //deprecated
  public SinkWriter<T> createWriter(final InitContext context) throws IOException {
    return null;
  }

  @Override
  public SinkWriter<T> createWriter(WriterInitContext context) {
    return new SinkWriter<T>() {
      @Override
      public void write(T element, Context context) {
        COLLECTED.add(element);
      }

      @Override
      public void flush(boolean endOfInput) {
        // No-op for this simple implementation
      }

      @Override
      public void close() throws Exception {
        // No-op for this simple implementation
      }
    };
  }

  public static <T> List<T> getCollectedElements() {
    return (List<T>) List.copyOf(COLLECTED);
  }

  public static void clear() {
    COLLECTED.clear();
  }
}
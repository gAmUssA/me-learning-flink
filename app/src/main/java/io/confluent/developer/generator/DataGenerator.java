package io.confluent.developer.generator;

import net.datafaker.Faker;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

public class DataGenerator {

  private static final Random RANDOM = new Random(System.currentTimeMillis());
  private static final Faker FAKER = new Faker();
  private static final String TIMEZONE = "UTC";
  private static final String SKY1_PREFIX = "SKY1";
  private static final String SUN_PREFIX = "SUN";

  private static String generateAirportCode() {
    return FAKER.aviation().airport();
  }

  private static String generateString(int size) {
    return FAKER.lorem().characters(size);
  }

  private static String generateEmail() {
    return FAKER.internet().emailAddress();
  }

  private static ZonedDateTime generateDepartureTime() {
    return LocalDate.now()
        .plusDays(RANDOM.nextInt(365))
        .atTime(RANDOM.nextInt(24), RANDOM.nextInt(60))
        .atZone(ZoneId.of(TIMEZONE));
  }

  private static ZonedDateTime generateArrivalTime(ZonedDateTime departure) {
    return departure
        .plusHours(RANDOM.nextInt(15))
        .plusMinutes(RANDOM.nextInt(60));
  }

  public static SkyOneAirlinesFlightData generateSkyOneAirlinesFlightData() {
    final ZonedDateTime departureTime = generateDepartureTime();
    final ZonedDateTime arrivalTime = generateArrivalTime(departureTime);
    return new SkyOneAirlinesFlightData(
        generateEmail(),
        departureTime,
        generateAirportCode(),
        arrivalTime,
        generateAirportCode(),
        SKY1_PREFIX + RANDOM.nextInt(1000),
        SKY1_PREFIX + generateString(6),
        500 + RANDOM.nextInt(1000),
        FAKER.aviation().aircraft(),
        generateEmail()
    );
  }

  public static SunsetAirFlightData generateSunsetAirFlightData() {
    final ZonedDateTime departureTime = generateDepartureTime();
    final ZonedDateTime arrivalTime = generateArrivalTime(departureTime);
    return new SunsetAirFlightData(
        generateEmail(),
        departureTime,
        generateAirportCode(),
        arrivalTime,
        generateAirportCode(),
        Duration.between(departureTime, arrivalTime),
        SUN_PREFIX + RANDOM.nextInt(1000),
        SUN_PREFIX + generateString(8),
        new BigDecimal(300 + RANDOM.nextInt(1500)),
        "Aircraft " + generateString(4)
    );
  }

  public record SunsetAirFlightData(
      String customerEmailAddress,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      ZonedDateTime departureTime,
      String departureAirport,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      ZonedDateTime arrivalTime,
      String arrivalAirport,
      Duration flightDuration,
      String flightId,
      String referenceNumber,
      BigDecimal totalPrice,
      String aircraftDetails
  ) {

  }

  public record SkyOneAirlinesFlightData(
      String emailAddress,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      ZonedDateTime flightDepartureTime,
      String iataDepartureCode,
      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
      ZonedDateTime flightArrivalTime,
      String iataArrivalCode,
      String confirmation,
      String flightNumber,
      float ticketPrice,
      String aircraft,
      String bookingAgencyEmail
  ) {

  }
}
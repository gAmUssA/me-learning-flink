plugins {
    application
    java
    id("com.gradleup.shadow") version "8.3.3"
}

repositories {
    mavenCentral()
}

group = "io.confluent.developer"
version = "0.1-SNAPSHOT"
description = "Flink Job"

repositories {
    mavenCentral()
}

val flinkVersion = "1.20.0"
val testcontainersVersion = "1.20.2"
val slf4jVersion = "1.7.36"
val log4jVersion = "2.17.1"
val jacksonVersion = "2.15.2"
val flinkKafkaVersion = "3.3.0-1.20"

configurations {
    create("flinkShadowJar")
}


dependencies {
    // Flink dependencies
    implementation("org.apache.flink:flink-java:$flinkVersion")
    implementation("org.apache.flink:flink-streaming-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    implementation("org.apache.flink:flink-json:$flinkVersion")
    implementation("org.apache.flink:flink-connector-base:$flinkVersion")
    implementation("org.apache.flink:flink-connector-kafka:$flinkKafkaVersion")
    // end Flink dependencies

    implementation("net.datafaker:datafaker:2.4.0")

    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-api:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")

    // Test dependencies
    // Flink Testing
    testImplementation("org.apache.flink:flink-test-utils:$flinkVersion")
    testImplementation("org.apache.flink:flink-connector-test-utils:$flinkVersion")
    // TC
    // TODO: add Kafka
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")


    testImplementation("org.assertj:assertj-core:3.21.0")
}
application {
    // Define the main class for the application.
    mainClass.set("io.confluent.developer.App")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets["main"].run {
    compileClasspath += configurations["flinkShadowJar"]
    runtimeClasspath += configurations["flinkShadowJar"]
}
sourceSets["test"].run {
    compileClasspath += configurations["flinkShadowJar"]
    runtimeClasspath += configurations["flinkShadowJar"]
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Built-By" to System.getProperty("user.name"),
            "Build-Jdk" to System.getProperty("java.version")
        )
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["flinkShadowJar"])
}
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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

//val flinkVersion = "1.20.0"
val flinkVersion = "1.19.1"
val testcontainersVersion = "1.20.2"
val slf4jVersion = "1.7.36"
val log4jVersion = "2.17.1"
val jacksonVersion = "2.15.2"
val flinkKafkaVersion = "3.3.0-1.20"

configurations {
    create("flinkShadowJar") {
        extendsFrom(configurations["implementation"])
        exclude(group = "org.apache.flink", module = "force-shading")
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "org.slf4j")
        exclude(group = "org.apache.logging.log4j")
    }
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
    mainClass.set("io.confluent.developer.movies.MovieStreamingJob")
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

tasks.withType<ShadowJar> {
    archiveClassifier.set("all")  // optional: adds classifier to jar name
    mergeServiceFiles()  // merges service files if any

    configurations = listOf(project.configurations["flinkShadowJar"])

    // ensures the manifest has the main class
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get(),
            "Built-By" to System.getProperty("user.name"),
            "Build-Jdk" to System.getProperty("java.version")
        )
    }
}

// Make the 'build' task depend on shadowJar
tasks.build {
    dependsOn(tasks.shadowJar)
}

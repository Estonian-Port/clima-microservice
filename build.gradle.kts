plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
}

group = "com.estonianport.clima"
version = "0.1.0"
description = "Clima microservice"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // WebClient para llamadas HTTP
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.prometheus:prometheus-metrics-core:1.0.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Encryption
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
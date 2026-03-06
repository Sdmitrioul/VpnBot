plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.5"
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
}

group = "com.dskroba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    //RocksDB dependency
    implementation("org.rocksdb:rocksdbjni:9.8.4")
    //Telegram bot library
    implementation("com.github.pengrad:java-telegram-bot-api:7.11.0")

    implementation("com.google.code.gson:gson")
    implementation("com.google.guava:guava:33.4.0-jre")
    //Generate QR codes
    implementation("com.google.zxing:javase:3.5.4")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
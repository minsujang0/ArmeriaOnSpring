plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("com.google.protobuf") version "0.9.2"
}

group = "io.github.minsujang0"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Querydsl
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    kapt(group = "com.querydsl", name = "querydsl-apt", classifier = "jakarta")

    // protobuf
    implementation("io.grpc:grpc-kotlin-stub:1.4.2")
    implementation("io.grpc:grpc-protobuf:1.70.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.5")
    implementation("com.google.protobuf:protobuf-java-util:3.25.5")

    // Armeria
    implementation(platform("com.linecorp.armeria:armeria-bom:1.32.5"))
    implementation("com.linecorp.armeria:armeria")
    implementation("com.linecorp.armeria:armeria-protobuf")
    implementation("com.linecorp.armeria:armeria-grpc")
    implementation("com.linecorp.armeria:armeria-grpc-kotlin")
    implementation("com.linecorp.armeria:armeria-kotlin")
    implementation("com.linecorp.armeria:armeria-spring-boot3-starter")
    implementation("com.linecorp.armeria:armeria-tomcat10")
    implementation("com.linecorp.armeria:armeria-spring-boot3-actuator-starter")
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

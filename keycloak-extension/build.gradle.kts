plugins {
    java
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    compileOnly("org.keycloak:keycloak-core:16.1.1")
    compileOnly("org.keycloak:keycloak-server-spi:16.1.1")
    compileOnly("org.keycloak:keycloak-server-spi-private:16.1.1")
    compileOnly("org.keycloak:keycloak-services:16.1.1")
    implementation("org.apache.kafka:kafka-clients:3.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testCompileOnly("org.keycloak:keycloak-core:16.1.1")
    testCompileOnly("org.keycloak:keycloak-server-spi:16.1.1")
    testCompileOnly("org.keycloak:keycloak-server-spi-private:16.1.1")
    testCompileOnly("org.keycloak:keycloak-services:16.1.1")
}

group = "org.example"
version = "1.0-SNAPSHOT"
description = "token-keycloak-api-extensions"
java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

// produce a fat jar
tasks.jar {
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .exclude(group = "org.slf4j")
        .map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.8"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.palantir.docker") version "0.33.0"
    id("com.palantir.docker-run") version "0.33.0"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

group = "org.taskmanager"
version = "0.0.3-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework:spring-jdbc") // for liquibase
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka:2.8.7")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.8")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:1.6.8")
    implementation("org.springdoc:springdoc-openapi-security:1.6.8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core:4.11.0")
    implementation("org.keycloak:keycloak-admin-client:18.0.0")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("io.r2dbc:r2dbc-postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("io.mockk:mockk:1.12.4")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("io.r2dbc:r2dbc-h2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

apply(plugin = "com.palantir.docker")

docker {
    name = "owahlen/task-service:".plus(version)
    uri("owahlen/task-service:".plus(version))
    tag("name", "task-service")
    buildArgs(mapOf("NAME" to "task-service", "BUILD_VERSION" to version.toString()))
    copySpec.from("build").into("build")
    pull(true)
    setDockerfile(file("Dockerfile"))
}

dockerRun {
    name = "task-service"
    image = "owahlen/task-service:".plus(version)
    ports("8080:8080")
}


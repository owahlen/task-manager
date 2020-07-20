FROM openjdk:11-jdk-slim
VOLUME /tmp
ARG DEPENDENCY=build
RUN echo ${DEPENDENCY}
COPY ${DEPENDENCY}/libs/task-service-${BUILD_VERSION}.jar /app/lib/task-service.jar
ENTRYPOINT ["java","-jar","/app/lib/task-service.jar"]

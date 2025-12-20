# Multi-stage build: build jar with Maven, then run on JRE
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copy entire backend to allow -pl reporting-service to resolve modules
COPY . .
RUN mvn -pl reporting-service -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/reporting-service/target/reporting-service-*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
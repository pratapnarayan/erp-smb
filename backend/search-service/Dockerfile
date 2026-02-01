FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -s .mvn/settings.xml -pl search-service -am -DskipTests clean package

# Spring Boot's repackage goal keeps a non-executable backup jar as *.jar.original.
# Ensure we copy the executable boot jar that contains a Main-Class manifest.
RUN set -eux; \
  JAR_PATH=$(ls -1 search-service/target/search-service-*.jar | grep -v '\\.jar\\.original$' | head -n 1); \
  cp "$JAR_PATH" /tmp/app.jar

FROM eclipse-temurin:21-jre

# Needed for docker-compose healthchecks (curl)
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/app/app.jar"]

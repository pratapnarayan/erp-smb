FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl auth-service -am -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/auth-service-*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]

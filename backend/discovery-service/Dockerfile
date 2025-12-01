FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/backend/discovery-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]

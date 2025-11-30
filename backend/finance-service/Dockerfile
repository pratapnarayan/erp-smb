FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY config-service ./config-service
COPY discovery-service ./discovery-service
COPY common-lib ./common-lib
COPY gateway-service ./gateway-service
COPY auth-service ./auth-service
COPY user-service ./user-service
COPY product-service ./product-service
COPY order-service ./order-service
COPY sales-service ./sales-service
COPY finance-service ./finance-service
COPY hrms-service ./hrms-service
COPY enquiry-service ./enquiry-service
RUN mvn -q -f pom.xml -pl finance-service -am package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/finance-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]

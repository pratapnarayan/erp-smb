FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/reporting-service-*.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
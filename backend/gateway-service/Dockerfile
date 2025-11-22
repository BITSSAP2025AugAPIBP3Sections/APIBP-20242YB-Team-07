FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/gateway-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]


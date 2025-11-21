FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/challenge-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY target/friseur-0.9.jar app.jar
RUN mkdir -p /var/log/friseur && chmod -R 777 /var/log/friseur
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-XX:MaxRAMPercentage=75.0", "app.jar"]
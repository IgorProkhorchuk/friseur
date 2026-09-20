FROM eclipse-temurin:25-jdk AS runtime
WORKDIR /app
COPY target/friseur-0.9.jar app.jar
RUN mkdir -p /var/log/friseur && chmod -R 777 /var/log/friseur
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-XX:MaxRAMPercentage=75.0", "app.jar"]

FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -DskipTests package && cp target/friseur-0.9.jar app.jar

FROM eclipse-temurin:25-jdk AS runtime-from-source
WORKDIR /app
COPY --from=build /app/app.jar app.jar
RUN mkdir -p /var/log/friseur && chmod -R 777 /var/log/friseur
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-XX:MaxRAMPercentage=75.0", "app.jar"]

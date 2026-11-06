FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B verify
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir /app/data && chown -R 10001:10001 /app
COPY --from=build /app/target/access-api-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

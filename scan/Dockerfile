FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/app/app-jar/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

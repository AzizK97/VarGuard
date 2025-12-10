FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
ENTRYPOINT ["java", "-jar", "app/app-jar/target/scan-app-jar-0.0.1-SNAPSHOT.jar"]

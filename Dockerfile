# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy Maven wrapper and project files
COPY . .

# Build the JAR
RUN ./mvnw clean package -DskipTests

# Use only the built JAR for the final image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

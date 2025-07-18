# Use Eclipse Temurin JDK 21 as build image
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# Copy Maven files and download dependencies (for better caching)
COPY pom.xml .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY . .

# Build the application
RUN ./mvnw clean package -DskipTests

# Use a smaller JRE image for running the app
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port (match your application port, e.g., 8700)
EXPOSE 8700

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
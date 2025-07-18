# Build stage: Compile the application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml first for better caching
COPY pom.xml .
# Download dependencies (will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src/

# Build the application
RUN mvn package -DskipTests

# Runtime stage: Setup the actual runtime environment
FROM bellsoft/liberica-openjre-debian:21-cds

# Add metadata
LABEL maintainer="AmaliTech Training Academy" \
    description="TalentRadar AI Analysis Service" \
    version="1.0"

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=8086
# AI Analysis Service specific environment variables
ENV AI_ANALYSIS_SERVICE_NAME=talentradar-ai-analysis
ENV AI_MODEL_ENDPOINT=
ENV AI_API_KEY=
ENV DATABASE_URL=
ENV DATABASE_USERNAME=
ENV DATABASE_PASSWORD=

# Create a non-root user
RUN useradd -r -u 1001 -g root aianalysis

WORKDIR /application

# Copy the extracted layers from the build stage
COPY --from=builder --chown=aianalysis:root /build/target/*.jar ./application.jar

# Configure container
USER 1001
EXPOSE 8086

# Use the standard JAR execution
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "application.jar"]
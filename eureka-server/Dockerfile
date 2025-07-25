FROM maven:3.9-amazoncorretto-17-alpine AS build

WORKDIR /app

# Copy pom.xml and source code (no service prefix needed)
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage - Using Amazon Corretto
FROM amazoncorretto:17-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8761

# Set JVM options for better resource usage
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
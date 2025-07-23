FROM maven:3.9.4-openjdk-17-slim AS build

WORKDIR /app

# Copy pom.xml and source code
COPY auth-service/pom.xml .
COPY auth-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Set JVM options for better resource usage
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
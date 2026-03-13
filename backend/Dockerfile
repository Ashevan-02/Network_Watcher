FROM eclipse-temurin:17-jdk-alpine

# Install Nmap for vulnerability scanning
RUN apk add --no-cache nmap

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/network-watcher-0.0.1-SNAPSHOT.jar"]

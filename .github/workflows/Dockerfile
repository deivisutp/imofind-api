# Dockerfile
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project files
COPY . .

# Build the project and run tests
RUN mvn clean install --batch-mode

# Run the application
CMD ["java", "-jar", "target/gcp-project-0.0.1.jar"]

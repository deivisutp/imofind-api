# Dockerfile
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy project files
COPY . .

# Build the project and run tests
RUN mvn clean install --batch-mode

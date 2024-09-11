# Use a Maven image with JDK 11 to build the project
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the project files
COPY . .

# Build the project (this will create the JAR file in the target directory)
RUN mvn clean install --batch-mode

# Use a minimal JDK image to run the application
FROM openjdk:11-jre-slim
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/gcp-project-0.0.1.jar app.jar

# Specify the command to run the application
CMD ["java", "-jar", "app.jar"]

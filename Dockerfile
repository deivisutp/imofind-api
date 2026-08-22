# Stage 1: Build the project
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the rest of the project files
COPY . .

# Build the project and create the JAR file (tests run in CI, not in the image build)
RUN mvn -B clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/imofind-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Specify the command to run the application
CMD ["java", "-jar", "app.jar"]
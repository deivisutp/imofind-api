FROM openjdk:11
EXPOSE 9000
ARG JAR_FILE=target/gcp-project-0.0.1.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
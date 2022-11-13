FROM openjdk:11-jdk-slim

ARG JAR_FILE=target/routing-service-app.jar
COPY ${JAR_FILE} routing-service-app.jar

EXPOSE 8050:8050

ENTRYPOINT ["java","-jar","/routing-service-app.jar"]
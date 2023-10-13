FROM ubuntu:latest

# APP
FROM openjdk:17.0-slim
ARG JAR_FILE=/api/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul","-jar","/app.jar"]
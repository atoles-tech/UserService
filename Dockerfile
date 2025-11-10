FROM eclipse-temurin:21-jdk-alpine

COPY /target/user-service-0.0.1-SNAPSHOT.jar /user-service/user-service.jar

WORKDIR /user-service

EXPOSE 8081

ENTRYPOINT [ "java","-jar","user-service.jar", "--spring.profiles.active=docker" ]
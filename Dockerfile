FROM openjdk:17-jdk
RUN groupadd spring && useradd spring -g spring
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV SERVER_PORT=8080
EXPOSE 8080/tcp
ENTRYPOINT ["java","-jar","/app.jar"]
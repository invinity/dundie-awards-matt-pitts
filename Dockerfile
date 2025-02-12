FROM openjdk:17-jdk
RUN groupadd spring && useradd spring -g spring
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV SERVER_PORT=80
EXPOSE 80/tcp
ENTRYPOINT ["java","-jar","/app.jar"]
FROM openjdk:8u102-jdk
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-jar","/app.jar"]

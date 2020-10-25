FROM openjdk:latest
ADD target/poller-latest.jar poller.jar
EXPOSE 9091/tcp
ENTRYPOINT ["java", "-jar", "poller.jar"]
FROM adoptopenjdk:11.0.7_10-jre-hotspot
VOLUME /tmp/thingverse-test-cassandra-app
ARG JAR_FILE=build/libs/*-boot.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

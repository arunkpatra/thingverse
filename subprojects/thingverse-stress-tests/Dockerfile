FROM adoptopenjdk:11.0.7_10-jre-hotspot
VOLUME /tmp/thingverse-stress-tests
ARG JAR_FILE=build/libs/*-gatling.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

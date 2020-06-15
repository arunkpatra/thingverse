FROM adoptopenjdk:11.0.7_10-jre-hotspot
ENV TZ=Asia/Kolkata
VOLUME /tmp/thingverse-admin
ARG JAR_FILE=build/libs/*-boot.jar
ARG RUN_SCRIPT=build/resources/main/run.sh
COPY ${JAR_FILE} /app.jar
COPY ./run.sh /
RUN ln -snf /user/share/zoneinfo/$TZ etc/localtime && echo $TZ > /etc/timezone && chmod +x /run.sh
ENTRYPOINT ["/run.sh"]
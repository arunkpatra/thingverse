FROM adoptopenjdk:11.0.7_10-jre-hotspot
ENV TZ=Asia/Kolkata
VOLUME /tmp/thingverse-backend
ARG JAR_FILE=build/libs/*-boot.jar
COPY ${JAR_FILE} /app.jar
COPY ./run.sh /
RUN ln -snf /user/share/zoneinfo/$TZ etc/localtime && echo $TZ > /etc/timezone && chmod +x /run.sh
ENTRYPOINT ["/run.sh"]

FROM amazoncorretto:17-alpine3.18 as builder-jre

RUN apk add --no-cache binutils

RUN $JAVA_HOME/bin/jlink \
         --module-path "$JAVA_HOME/jmods" \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /jre

#----------------------------------------
FROM alpine:3.18.4

MAINTAINER Psam1017
EXPOSE 8080

ENV TZ=Asia/Seoul
ENV JAVA_HOME=/jre
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV APP_PATH=/home/ubuntu/app
ENV APP_PROFILE=stage

COPY --from=builder-jre /jre $JAVA_HOME

RUN apk add --no-cache tzdata
RUN apk add --no-cache bash
RUN mkdir -p $APP_PATH
WORKDIR $APP_PATH
COPY build/libs/*SNAPSHOT.jar app.jar

ENTRYPOINT java -jar app.jar --spring.profiles.active=$APP_PROFILE
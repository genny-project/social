FROM openjdk:8u131-jre-alpine
RUN apk update && apk add jq && apk add curl && apk add bash

ADD target/social-fat.jar /service.jar
#ADD cluster.xml /cluster.xml

RUN mkdir /realm
ADD realm /opt/realm
ADD docker-entrypoint.sh /docker-entrypoint.sh

WORKDIR /

EXPOSE 5701
EXPOSE 5702
EXPOSE 5703
EXPOSE 5704
EXPOSE 5705
EXPOSE 5706
EXPOSE 8088
EXPOSE 8080
EXPOSE 15701
EXPOSE 15702
EXPOSE 15703
EXPOSE 15704

HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD curl -f / http://localhost:8080/version || exit 1

ENTRYPOINT [ "/docker-entrypoint.sh" ]


FROM openjdk:8u131-jre-alpine
RUN apk update && apk add jq

ADD target/social-0.0.1-SNAPSHOT-fat.jar /service.jar


ADD realm /realm
ADD docker-entrypoint.sh /docker-entrypoint.sh

WORKDIR /

EXPOSE 5707


#CMD ["java"]
ENTRYPOINT [ "/docker-entrypoint.sh" ]


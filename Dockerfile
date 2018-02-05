FROM  openjdk:8u151-jre-alpine3.7
RUN echo http://mirror.yandex.ru/mirrors/alpine/v3.7/main > /etc/apk/repositories; \
    echo http://mirror.yandex.ru/mirrors/alpine/v3.7/community >> /etc/apk/repositories

RUN apk update && apk add jq && apk add bash

ADD target/social-0.0.1-SNAPSHOT-fat.jar /service.jar


ADD realm /realm
ADD docker-entrypoint.sh /docker-entrypoint.sh

WORKDIR /

EXPOSE 5707


#CMD ["java"]
ENTRYPOINT [ "/docker-entrypoint.sh" ]


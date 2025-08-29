FROM sbtscala/scala-sbt:amazoncorretto-al2023-21.0.8_1.11.5_3.7.2

COPY . /app

WORKDIR /app

RUN sbt assembly

ENTRYPOINT ["java", "-jar", "web/target/scala-3.7.2/sms-validator-web.jar"]
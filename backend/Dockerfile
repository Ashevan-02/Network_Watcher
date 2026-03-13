FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache nmap

WORKDIR /app

COPY target/network-watcher-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

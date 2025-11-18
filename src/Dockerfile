FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . /app

RUN ./mvnw clean package

EXPOSE 8080

CMD ["java", "-jar", "target/spring-rest-demo-0.0.1-SNAPSHOT.jar"]

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . /app

RUN chmod +x mvnw
RUN ./mvnw clean package

EXPOSE 8080

CMD java -jar $(find target -name "*.jar" | head -n 1)

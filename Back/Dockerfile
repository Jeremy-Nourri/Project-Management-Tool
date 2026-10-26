FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY lombok.config .
COPY src ./src

RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S pmt && adduser -S pmt -G pmt

WORKDIR /app

COPY --from=build /build/target/pmt-backend-*.jar app.jar

RUN chown -R pmt:pmt /app
USER pmt

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

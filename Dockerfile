FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -q -DskipTests=false test package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV APP_VERSION=dev
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

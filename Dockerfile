FROM eclipse-temurin:21

WORKDIR /app

COPY target/library-1.jar /app/library.jar

ENTRYPOINT ["java", "-jar", "library.jar"]

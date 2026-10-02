FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/control-vehiculos-1.0.jar app.jar
ENV VEHICULOS_DB_PATH=/data/vehiculos.db
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

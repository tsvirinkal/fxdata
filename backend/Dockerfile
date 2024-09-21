FROM eclipse-temurin:17-jre-ubi9-minimal
WORKDIR /fxdata
RUN mkdir certs
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} fxdata.jar
COPY ./init-data/data.sql data.sql
CMD ["java","-jar","fxdata.jar"]
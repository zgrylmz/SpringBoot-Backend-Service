##FROM eclipse-temurin:17-jre-alpine
##WORKDIR /app
##COPY target/*.jar app.jar
##EXPOSE 8080
##ENTRYPOINT ["java","-jar","app.jar"]
#
#
#
## 1) Build stage: Maven + JDK 21    ESKI STAGE 
#FROM maven:3.9-eclipse-temurin-21 AS build
#WORKDIR /app
#
#COPY pom.xml .
#RUN mvn -B -DskipTests dependency:go-offline
#
#COPY src ./src
#RUN mvn -B -DskipTests clean package
#
## 2) Runtime stage: sadece çalıştırmak için JRE 21
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#
#COPY --from=build /app/target/*SNAPSHOT.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","/app/app.jar"]




# ========================
# DEV IMAGE (development)
# ========================
FROM maven:3.9-eclipse-temurin-21 AS dev
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline  
##Kod bir kere indirilir daha sonra cache sayesinde tekrar tekrar indirilmez 

COPY src ./src

EXPOSE 8080 5005

#ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

CMD ["mvn", "spring-boot:run"]



# ========================
# BUILD IMAGE
# ========================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests clean package


# ========================
# PROD IMAGE
# ======================== Runtime Stage
FROM eclipse-temurin:21-jre AS prod
WORKDIR /app

COPY --from=build /app/target/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

FROM maven as builder

COPY src  /app/src
COPY pom.xml /app

RUN mvn -f /app/pom.xml package



FROM bellsoft/liberica-openjdk-debian

COPY --from=builder /app/target/asterisk-java-server-1.0-SNAPSHOT.jar /app/java-server.jar

ENTRYPOINT ["java", "-jar" ,"/app/java-server.jar"]

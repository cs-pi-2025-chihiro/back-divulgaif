FROM eclipse-temurin:21-jdk-jammy AS build
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/ ./src/
COPY keystore.p12 /app/keystore.p12
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r divulgaifuser && \
    useradd -r -g divulgaifuser -m -d /home/divulgaifuser divulgaifuser && \
    mkdir -p /app && \
    chown -R divulgaifuser:divulgaifuser /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/keystore.p12 /app/keystore.p12
RUN chown divulgaifuser:divulgaifuser /app/app.jar && \
    chown divulgaifuser:divulgaifuser /app/keystore.p12
USER divulgaifuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f https://localhost:8080/actuator/health --insecure || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=70", "-jar", "/app/app.jar"]

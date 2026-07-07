# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy the whole repository
COPY . .

# Build protocol
WORKDIR /app/protocol
RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests

# Build server
WORKDIR /app/server
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/server/target/TunnelFlowServer-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
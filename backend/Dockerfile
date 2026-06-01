# ── Etapa 1: compilación ──────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copiar el wrapper de Maven y el POM primero para aprovechar la caché de capas
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src src
RUN ./mvnw clean package -DskipTests

# ── Etapa 2: ejecución ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/irpfhelper-0.0.1-SNAPSHOT.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]

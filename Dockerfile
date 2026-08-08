# Multi-stage build per ARCHITECTURE.md: one image, one process, one port -
# Angular's static build ends up served by the same Spring Boot process
# that serves the API, so there's no CORS and nothing to keep in sync.

# ---- Stage 1: build Angular ----
FROM node:20-slim AS web-build
WORKDIR /web
COPY web/package*.json ./
RUN npm ci
COPY web/ ./
RUN npm run build -- --configuration production

# ---- Stage 2: build Spring Boot, embedding the Angular build as static resources ----
FROM maven:3.9-eclipse-temurin-21 AS api-build
WORKDIR /api
COPY api/pom.xml ./
RUN mvn -B dependency:go-offline
COPY api/src ./src
COPY --from=web-build /web/dist/salary-management-frontend ./src/main/resources/static
RUN mvn -B -DskipTests package

# ---- Stage 3: slim runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=api-build /api/target/salary-management.jar app.jar

# SQLite file lives on a mountable volume so it survives container restarts
# (see ARCHITECTURE.md's deployment section on ephemeral-filesystem hosts).
VOLUME ["/app/data"]
ENV SALARY_DB_PATH=/app/data/salary.db

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

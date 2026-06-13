FROM node:22-bookworm-slim AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /app
COPY backend/ ./backend/
COPY --from=frontend-build /app/frontend/dist ./backend/src/main/resources/static
RUN ./backend/gradlew -p backend bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

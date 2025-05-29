# Use OpenJDK 21 as base image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install curl for health check
RUN apk add --no-cache curl

# Create app user for security
RUN addgroup -S appuser && adduser -S -G appuser appuser

# Copy the startup script
COPY src/main/script/start-app.sh /app/start-app.sh

# Copy the JAR file from Maven build output
COPY target/aieng-1.0-SNAPSHOT.jar /app/app.jar

# Make startup script executable
RUN chmod +x /app/start-app.sh

# Change ownership to app user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use startup script as entrypoint
ENTRYPOINT ["/app/start-app.sh"]
#!/bin/sh

# Spring Boot application startup script with optimized JVM options

# Exit on any error
set -e

# Default values for memory settings (can be overridden via environment variables)
# Optimized for containers with ~500MB total memory
HEAP_SIZE=${HEAP_SIZE:-256m}
MAX_HEAP_SIZE=${MAX_HEAP_SIZE:-350m}
METASPACE_SIZE=${METASPACE_SIZE:-64m}

echo "Starting Spring Boot application..."
echo "Heap size: $HEAP_SIZE"
echo "Max heap size: $MAX_HEAP_SIZE"
echo "Metaspace size: $METASPACE_SIZE"

# Java options for production deployment
JAVA_OPTS="-server \
-Xms$HEAP_SIZE \
-Xmx$MAX_HEAP_SIZE \
-XX:MetaspaceSize=$METASPACE_SIZE \
-XX:+UseContainerSupport \
-XX:+UseSerialGC \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+DisableExplicitGC \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:CompileThreshold=1500 \
-Xshare:on \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/app/logs/ \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
-Dfile.encoding=UTF-8 \
-Duser.timezone=${TZ:-UTC}"

# Additional JVM options for debugging (only if DEBUG is set)
if [ "$DEBUG" = "true" ]; then
    echo "Debug mode enabled"
    JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
fi

# Create logs directory if it doesn't exist
mkdir -p /app/logs

# Start the application
exec java $JAVA_OPTS -jar /app/app.jar "$@"
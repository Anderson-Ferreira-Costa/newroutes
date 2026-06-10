#!/bin/sh
# Gradle wrapper script
# Download Gradle distribution and run it

GRADLE_WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
GRADLE_HOME="${HOME}/.gradle/wrapper/dists"

exec java -jar "$GRADLE_WRAPPER_JAR" "$@"

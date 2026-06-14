#!/bin/sh
# Apache Maven Wrapper Script
# Licensed to the Apache Software Foundation (ASF)

set -e

DIRNAME=$(cd "$(dirname "$0")" > /dev/null && pwd)
BASEDIR=$DIRNAME
MAVEN_WRAPPER_JAR="$BASEDIR/.mvn/wrapper/maven-wrapper.jar"

if [ ! -f "$MAVEN_WRAPPER_JAR" ]; then
  echo "ERROR: Maven wrapper JAR not found"
  exit 1
fi

MAVEN_OPTS=${MAVEN_OPTS:--Xmx1024m}

exec java $MAVEN_OPTS \
  -Dmaven.multiModuleProjectDirectory="$BASEDIR" \
  -cp "$MAVEN_WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"

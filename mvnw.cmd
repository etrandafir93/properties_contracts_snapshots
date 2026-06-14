@REM Apache Maven Wrapper
@REM Licensed to the Apache Software Foundation (ASF)
@echo off
setlocal enabledelayedexpansion

for %%i in ("%~dp0\.") do set BASEDIR=%%~fi

set MAVEN_WRAPPER_JAR=%BASEDIR%\.mvn\wrapper\maven-wrapper.jar

if not exist "%MAVEN_WRAPPER_JAR%" (
    echo ERROR: Maven wrapper JAR not found: %MAVEN_WRAPPER_JAR%
    exit /b 1
)

set MAVEN_OPTS=%MAVEN_OPTS% -Xmx1024m

java %MAVEN_OPTS% ^
  -Dmaven.multiModuleProjectDirectory="%BASEDIR%" ^
  -cp "%MAVEN_WRAPPER_JAR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

endlocal & exit /b %ERRORLEVEL%

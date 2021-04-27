
@echo off

REM Run the server using the system JVM

set DIR=%~dp0
java --module-path %DIR%\bin -m j9fs/com.github.anastasop.j9fs.server.Server %*

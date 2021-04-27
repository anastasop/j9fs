
@echo off

@REM Create a JVM tailored for the j9fs module. Use jre\bin\jre.bat to run the server

CALL build.cmd

rd /Q /S jre

jlink --launcher jre=j9fs/com.github.anastasop.j9fs.server.Server --module-path "%JAVA_HOME%\jmods";bin --add-modules j9fs --output jre

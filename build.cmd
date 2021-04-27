
@echo off

@REM Build the j9fs module

javac -d bin --module-source-path src --module j9fs

copy /Y src\j9fs\com\github\anastasop\j9fs\server\logging.properties bin\j9fs\com\github\anastasop\j9fs\server >NUL

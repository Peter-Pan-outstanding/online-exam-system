@echo off
rem Run the console online exam system
cd /d "%~dp0"
java -cp "classes;lib\mysql-connector-java-8.0.33.jar;." com.group.exam.Main
pause

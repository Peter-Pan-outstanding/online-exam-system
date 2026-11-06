@echo off
cd /d "%~dp0"
if not exist classes mkdir classes
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -source 8 -target 8 -d classes @sources.txt
if errorlevel 1 (
    del sources.txt
    exit /b 1
)
del sources.txt
echo Compile done. Run: run.bat

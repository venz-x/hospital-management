@echo off
REM Compiles every .java file into out\ and starts the Swing application.
cd /d "%~dp0"

if not exist out mkdir out

echo Compiling...
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
del sources.txt
if errorlevel 1 exit /b 1

echo Starting Hospital Management System...
java -cp out Main %*

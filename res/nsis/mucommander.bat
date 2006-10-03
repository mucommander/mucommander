@echo off
echo.
echo -------------------------
echo - Launching muCommander -
echo -------------------------
echo.

if "%JAVA_HOME%"=="" goto nojavahome
echo Found a Java runtime in %JAVA_HOME%
start "%JAVA_HOME%\bin\javaw.exe -jar" mucommander.jar
goto exit

:nojavahome
echo JAVA_HOME environment variable not set, trying default javaw.exe
start javaw -jar mucommander.jar
goto exit

:exit
exit

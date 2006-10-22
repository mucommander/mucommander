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
start javaw -jar mucommander.jar %1% %2% %3% %4% %5% %6% %7% %8% %9%
goto exit

:exit
exit

@REM ----------------------------------------------------------------------------
@REM This script launches a Thingverse node.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Thingverse Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM THINGVERSE_OPTS - parameters passed to the application
@REM ----------------------------------------------------------------------------

@echo off

@setlocal

set ERROR_CODE=0

@REM To isolate internal variables from possible post scripts, we use another setlocal
@setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo Error: JAVA_HOME not found in your environment. >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Error: JAVA_HOME is set to an invalid directory. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo Please set the JAVA_HOME variable in your environment to match the >&2
echo location of your Java installation. >&2
echo.
goto error

@REM ==== END VALIDATION ====

:init

set APP_INSTALLATION_DIR=C:\Users\M1020513\.m2\repository
set APP_NAME=thingverse-api
set APP_DIRECTORY=com\thingverse
set APP_VERSION=0.0.1
set APP_PACKAGING=jar

@rem echo Starting application `%APP_NAME%` ...
title Thingverse - %APP_NAME%
SET THINGVERSE_JAVA="%JAVA_HOME%bin\java.exe"
SET THINGVERSE_APP=%APP_INSTALLATION_DIR%\%APP_DIRECTORY%\%APP_NAME%\%APP_VERSION%\%APP_NAME%-%APP_VERSION%.%APP_PACKAGING%
SET THINGVERSE_OPTS=

%THINGVERSE_JAVA% %THINGVERSE_OPTS% -jar %THINGVERSE_APP%

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

exit /B %ERROR_CODE%
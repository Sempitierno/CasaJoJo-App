@rem Script de arranque de Gradle Wrapper para Windows (version simplificada).
@rem Descarga y usa la version de Gradle indicada en gradle\wrapper\gradle-wrapper.properties.

@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if exist "%CLASSPATH%" goto findJava

echo ERROR: falta el archivo gradle\wrapper\gradle-wrapper.jar 1>&2
echo. 1>&2
echo Este archivo binario no pudo incluirse en el ZIP. Opciones para restaurarlo: 1>&2
echo   1) Abrir el proyecto con Android Studio (Open ^> carpeta CasaJoJo). La sincronizacion 1>&2
echo      de Gradle funciona sin este archivo y descarga Gradle 8.9 automaticamente. 1>&2
echo   2) Con Gradle 8.9 instalado: gradle wrapper --gradle-version 8.9 1>&2
echo   3) Copiar gradle-wrapper.jar desde otro proyecto Android Studio a gradle\wrapper\ 1>&2
echo Consulte la seccion "Gradle Wrapper" del README.md. 1>&2
goto fail

:findJava
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo ERROR: JAVA_HOME no esta definido y no se encontro el comando 'java' en el PATH. 1>&2
echo Instale un JDK 17 o superior y configure JAVA_HOME. 1>&2
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo ERROR: JAVA_HOME apunta a un directorio invalido: %JAVA_HOME% 1>&2
goto fail

:execute
"%JAVA_EXE%" -Xmx64m -Xms64m "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
exit /b 1

:mainEnd
endlocal

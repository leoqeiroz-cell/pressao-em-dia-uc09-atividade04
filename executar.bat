@echo off
cd /d "%~dp0"
java -jar PressaoEmDia.jar
if errorlevel 1 (
  echo Nao foi possivel iniciar. Verifique se o Java 17 ou superior esta instalado.
  pause
)

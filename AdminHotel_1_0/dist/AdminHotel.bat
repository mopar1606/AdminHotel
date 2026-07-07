@echo off
title HOTEL LAS TERRAZAS II
java -splash:Fondo.png -Dsun.awt.noerasebackground=true -jar AdminHotel.jar
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: No se pudo iniciar la aplicacion.
    echo Asegurese de tener Java 8 instalado.
    pause
)

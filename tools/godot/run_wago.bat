@echo off
setlocal
set ROOT=%~dp0..\..
set GODOT=C:\Softwares\Godot_v4.7-stable_win64.exe
if not exist "%GODOT%" set GODOT=C:\Softwares\Godot_v4.7-stable_win64_console.exe
if not exist "%GODOT%" (
  echo Godot was not found in C:\Softwares.
  echo Edit this file and set GODOT to your Godot executable.
  pause
  exit /b 1
)
"%GODOT%" --path "%ROOT%\godot"

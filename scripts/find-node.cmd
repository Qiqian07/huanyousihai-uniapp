@echo off
rem ===========================================================================
rem  Locate node.exe without relying on PATH.
rem  Many machines have node bundled somewhere (or none at all), so we probe
rem  the usual install locations, then PATH, then the common bundle dirs.
rem
rem  Output:  NODE      full path to node.exe
rem           NODE_DIR  its directory
rem           NPM       full path to npm.cmd (falls back to "npm")
rem  Returns errorlevel 1 when nothing usable is found.
rem ===========================================================================
if defined NODE exit /b 0

if exist "%ProgramFiles%\nodejs\node.exe" set "NODE=%ProgramFiles%\nodejs\node.exe"
if not defined NODE if exist "%LocalAppData%\Programs\nodejs\node.exe" set "NODE=%LocalAppData%\Programs\nodejs\node.exe"
if not defined NODE if exist "%USERPROFILE%\scoop\apps\nodejs\current\node.exe" set "NODE=%USERPROFILE%\scoop\apps\nodejs\current\node.exe"
if not defined NODE if exist "%APPDATA%\nvm\current\node.exe" set "NODE=%APPDATA%\nvm\current\node.exe"

if not defined NODE for /f "delims=" %%P in ('where node 2^>nul') do if not defined NODE set "NODE=%%P"

if not defined NODE for /d %%D in ("%USERPROFILE%\.workbuddy\binaries\node\versions\*") do if exist "%%~D\node.exe" set "NODE=%%~D\node.exe"

if not defined NODE (
  echo.
  echo   [x] Node.js not found on this machine.
  echo.
  echo       Install Node.js LTS first ^(then reopen this file^):
  echo         https://nodejs.org/zh-cn/download
  echo.
  echo       Note: the "node" command must be available. If you just
  echo       installed it, close ALL terminal windows and try again.
  echo.
  exit /b 1
)

for %%I in ("%NODE%") do set "NODE_DIR=%%~dpI"
if exist "%NODE_DIR%npm.cmd" (set "NPM=%NODE_DIR%npm.cmd") else (set "NPM=npm")
exit /b 0

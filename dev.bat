@echo off
chcp 65001 >nul
cd /d "%~dp0"

call "%~dp0scripts\find-node.cmd"
if errorlevel 1 (
  pause
  exit /b 1
)

echo.
echo   Huan You Si Hai V1.0 - dev mode (hot reload)
echo   ------------------------------------------------
echo   Node: %NODE%
echo.

if not exist "node_modules" (
  echo   [!] node_modules not found. Installing dependencies, this takes a while...
  echo.
  call "%NPM%" install
  if errorlevel 1 (
    echo.
    echo   [x] Install failed. Try the mirror:
    echo       npm config set registry https://registry.npmmirror.com
    pause
    exit /b 1
  )
)

echo   Open http://localhost:5180 after it compiles.
echo.
call "%NPM%" run dev:h5
pause

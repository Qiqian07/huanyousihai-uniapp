@echo off
chcp 65001 >nul
cd /d "%~dp0"

call "%~dp0scripts\find-node.cmd"
if errorlevel 1 (
  pause
  exit /b 1
)

echo.
echo   Huan You Si Hai V1.0 - local preview
echo   ------------------------------------------------
echo   Node: %NODE%
echo.

if not exist "dist\build\h5\index.html" (
  echo   [!] No build output found. Building now, ~30s...
  echo.
  call "%NPM%" run build:h5
  if errorlevel 1 (
    echo.
    echo   [x] Build failed. See the messages above.
    pause
    exit /b 1
  )
)

"%NODE%" serve.mjs
pause

@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo.
echo   Huan You Si Hai V1.0 - local preview
echo   (build output must exist: run  npm run build:h5  first)
echo.
node serve.mjs
pause

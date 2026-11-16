@echo off
chcp 65001 >nul
cd /d "%~dp0"

where git >nul 2>nul
if errorlevel 1 (
  echo.
  echo   [x] git not found. Install Git for Windows first:
  echo       https://git-scm.com/download/win
  echo.
  pause
  exit /b 1
)

if "%~1"=="" (
  echo.
  echo   Usage:
  echo     push.bat https://github.com/YOUR_NAME/YOUR_REPO.git
  echo.
  pause
  exit /b 1
)

echo.
echo   Remote: %~1
echo.

git remote remove origin 2>nul
git remote add origin %~1
git -c http.schannelCheckRevoke=false push -u origin main

if errorlevel 1 (
  echo.
  echo   [x] Push failed. Common causes:
  echo       - network blocked: turn on your proxy / VPN, then retry
  echo       - certificate revocation error: already handled by this script
  echo       - auth expired: run  gh auth login
  echo.
) else (
  echo.
  echo   [ok] Pushed.
  echo.
)

pause

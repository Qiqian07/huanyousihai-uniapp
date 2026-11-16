@echo off
chcp 65001 >nul
cd /d "%~dp0"

if "%~1"=="" (
  echo.
  echo   Usage:
  echo     push.bat https://github.com/YOUR_NAME/YOUR_REPO.git
  echo.
  echo   Create an empty repo on github.com first ^(no README, no .gitignore^).
  echo.
  pause
  exit /b 1
)

echo.
echo   Remote: %~1
echo.

git remote remove origin 2>nul
git remote add origin %~1
git push -u origin main

if errorlevel 1 (
  echo.
  echo   [x] Push failed. Check network / login:
  echo       - behind GFW? turn on your proxy first
  echo       - auth expired? run:  gh auth login
  echo.
) else (
  echo.
  echo   [ok] Pushed.
  echo.
)

pause

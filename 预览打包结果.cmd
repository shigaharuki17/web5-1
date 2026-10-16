@echo off
cd /d "%~dp0"
if not exist dist\index.html (
  echo Please run npm run build first.
  pause
  exit /b 1
)
node preview.js
pause

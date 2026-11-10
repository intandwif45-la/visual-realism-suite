@echo off
REM Gradle wrapper (Windows) - memakai Gradle yang terinstall di laptop.
where gradle >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
  echo Gradle tidak ditemukan. Install Gradle atau JDK 17 + Gradle dulu (lihat README.md).
  exit /b 1
)
gradle %*

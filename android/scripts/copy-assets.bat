@echo off
setlocal enabledelayedexpansion
echo ========================================
echo  PC Hardware Advisor - Copy Web Assets
echo ========================================
echo.

set SRC=E:\data\Desktop\IdeaProjects\backend\src\main\resources\static
set DST=E:\data\Desktop\IdeaProjects\android\app\src\main\assets\www

echo Source: %SRC%
echo Destination: %DST%
echo.

if not exist "%SRC%" (
    echo [ERROR] Source directory not found: %SRC%
    echo Please build the frontend first: cd frontend\vue-project ^&^& npm run build
    pause
    exit /b 1
)

echo [1/2] Cleaning destination...
if exist "%DST%" rmdir /s /q "%DST%"
mkdir "%DST%"

echo [2/2] Copying files...
xcopy "%SRC%\*" "%DST%\" /E /H /Y /Q

echo.
echo ========================================
echo  Done! Assets copied successfully.
echo ========================================
pause

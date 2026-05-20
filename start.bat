@echo off
chcp 65001 >nul
title PC硬件推荐系统

echo =============================================
echo   PC硬件推荐系统 - 外网访问模式
echo =============================================
echo.

REM 清理旧进程
echo [0] 清理旧进程...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING" 2^>nul') do (
    taskkill /f /pid %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING" 2^>nul') do (
    taskkill /f /pid %%a >nul 2>&1
)
echo   旧进程已清理
echo.

REM 检查是否需要构建前端
if not exist "backend\src\main\resources\static\assets\index-*.js" (
    echo [1] 构建前端...
    cd /d "%~dp0frontend\vue-project"
    call npm run build-only 2>nul
    if errorlevel 1 (
        echo   前端构建失败，请检查 !
        pause
        exit /b 1
    )
    cd /d "%~dp0"
    echo   前端构建完成
    echo.
)

REM 启动后端
echo [2] 启动后端 (端口 8080)...
start "PCAdvisor-Backend" cmd /c "cd /d %~dp0backend && mvnw.cmd spring-boot:run"
echo   后端启动中，请等待约15秒...
echo.

timeout /t 15 /nobreak >nul

echo =============================================
echo   系统已启动！
echo.
echo   本机访问:  http://localhost:8080
echo =============================================
echo.
echo   如需外网访问，请打开新终端并运行:
echo     npx bore local 8080 --to bore.pub
echo.
echo   或者使用 ngrok (更稳定):
echo     ngrok http 8080
echo.
echo   关闭此窗口将停止后端服务
echo =============================================
pause

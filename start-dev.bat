@echo off
chcp 65001 >nul
title PC硬件推荐系统 - 开发模式

echo =============================================
echo   PC硬件推荐系统 - 本地开发模式
echo =============================================
echo.

REM 清理旧进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING" 2^>nul') do (
    taskkill /f /pid %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING" 2^>nul') do (
    taskkill /f /pid %%a >nul 2>&1
)

echo [1/2] 启动后端 Spring Boot (端口 8080)...
start "Backend" cmd /c "cd /d %~dp0backend && mvnw.cmd spring-boot:run"
echo   后端启动中...

timeout /t 12 /nobreak >nul

echo [2/2] 启动前端 Vite (端口 5173)...
start "Frontend" cmd /c "cd /d %~dp0frontend\vue-project && npm run dev"
echo   前端启动中...

timeout /t 5 /nobreak >nul

echo.
echo =============================================
echo   启动完成！
echo   本机访问:    http://localhost:5173
echo   局域网访问:  http://你的IP地址:5173
echo.
echo   外网访问请在终端运行:
echo     npx bore local 5173 --to bore.pub
echo =============================================
pause

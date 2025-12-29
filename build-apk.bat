@echo off
REM ============================================================
REM Android AI Assistant APK 构建脚本
REM ============================================================

echo ==========================================
echo   Android AI Assistant APK 构建工具
echo ==========================================
echo.

REM 检查 Android SDK
if not defined ANDROID_HOME (
    echo [错误] 未找到 ANDROID_HOME 环境变量
    echo 请设置 ANDROID_HOME 指向 Android SDK 路径
    pause
    exit /b 1
)

echo [1/5] 检查环境...
echo ANDROID_HOME: %ANDROID_HOME%
echo.

REM 检查 Gradle
where gradle >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未找到 Gradle
    echo 请安装 Gradle 或使用 Android Studio 构建
    pause
    exit /b 1
)

echo [2/5] 清理之前的构建...
cd /d "%~dp0"
if exist app\build (
    rmdir /s /q app\build
)
echo 清理完成
echo.

echo [3/5] 编译项目...
call gradlew clean assembleRelease
if %errorlevel% neq 0 (
    echo [错误] 编译失败
    pause
    exit /b 1
)
echo 编译完成
echo.

echo [4/5] 查找 APK...
set APK_PATH=
for /r %%f in (app\build\outputs\apk\release\*.apk) do (
    set APK_PATH=%%f
)

if "%APK_PATH%"=="" (
    echo [错误] 未找到 APK 文件
    pause
    exit /b 1
)

echo APK 路径: %APK_PATH%
echo.

echo [5/5] 复制 APK 到当前目录...
copy "%APK_PATH%" "AI-Assistant-Release.apk" >nul
echo.

echo ==========================================
echo   构建完成！
echo ==========================================
echo.
echo APK 文件: AI-Assistant-Release.apk
echo.
echo 安装说明：
echo 1. 将 APK 传输到手机
echo 2. 在手机上安装（需要允许未知来源）
echo 3. 首次运行授予 root 权限
echo 4. 下载 AI 模型开始使用
echo.
pause
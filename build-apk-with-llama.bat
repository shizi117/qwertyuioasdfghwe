@echo off
REM ============================================================
REM Android AI Assistant APK 构建脚本（包含 llama.cpp）
REM ============================================================

echo ==========================================
echo   Android AI Assistant 完整构建工具
echo   包含 llama.cpp 本地推理引擎
echo ==========================================
echo.

cd /d "%~dp0"

REM 步骤 1: 下载并编译 llama.cpp
echo [1/6] 准备 llama.cpp...
if not exist llama.cpp (
    echo 克隆 llama.cpp 仓库...
    git clone https://github.com/ggerganov/llama.cpp.git
    cd llama.cpp
    echo.
    echo 编译 llama.cpp for Android ARM64...
    call android-build-arm64.bat
    cd ..
) else (
    echo llama.cpp 已存在，跳过下载
)
echo.

REM 步骤 2: 复制编译好的二进制文件
echo [2/6] 复制 llama.cpp 二进制文件...
if not exist app\src\main\jniLibs\arm64-v8a (
    mkdir app\src\main\jniLibs\arm64-v8a
)
copy llama.cpp\build-android\arm64-v8a\bin\main app\src\main\jniLibs\arm64-v8a\libllama.so >nul 2>nul
if %errorlevel% neq 0 (
    echo [警告] 未找到预编译的 llama.cpp，将在运行时动态加载
)
echo.

REM 步骤 3: 准备模型下载脚本
echo [3/6] 准备模型下载脚本...
mkdir app\src\main\assets 2>nul
(
echo #!/system/bin/sh
echo # 模型下载脚本
echo.
echo MODELS_DIR="/sdcard/Android/data/com.android.aiassistant/files/models"
echo mkdir -p "$MODELS_DIR"
echo.
echo echo "=========================================="
echo echo "  AI 模型下载"
echo echo "=========================================="
echo echo.
echo echo "选择要下载的模型："
echo echo "1. Qwen2.5-7B-Instruct (推荐)"
echo echo "2. Phi-4-mini (轻量级)"
echo echo "3. DeepSeek-Coder (代码生成)"
echo echo "4. 退出"
echo echo.
echo read -p "请选择 [1-4]: " choice
echo.
echo case $choice in
echo     1^)
echo         echo "下载 Qwen2.5-7B-Instruct..."
echo         cd "$MODELS_DIR"
echo         wget https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf
echo         ;;
echo     2^)
echo         echo "下载 Phi-4-mini..."
echo         cd "$MODELS_DIR"
echo         wget https://huggingface.co/microsoft/Phi-4-mini-instruct-GGUF/resolve/main/Phi-4-mini-instruct-Q4_K_M.gguf
echo         ;;
echo     3^)
echo         echo "下载 DeepSeek-Coder..."
echo         cd "$MODELS_DIR"
echo         wget https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct-GGUF/resolve/main/deepseek-coder-v2-lite-instruct-q4_k_m.gguf
echo         ;;
echo     4^)
echo         echo "退出"
echo         exit 0
echo         ;;
echo     *^)
echo         echo "无效选择"
echo         exit 1
echo         ;;
echo esac
echo.
echo echo "下载完成！"
echo echo 模型保存在: $MODELS_DIR
) > app\src\main\assets\download_models.sh
echo.

REM 步骤 4: 构建 APK
echo [4/6] 构建 APK...
call gradlew clean assembleRelease
if %errorlevel% neq 0 (
    echo [错误] 构建失败
    pause
    exit /b 1
)
echo.

REM 步骤 5: 查找并复制 APK
echo [5/6] 处理 APK 文件...
set APK_PATH=
for /r %%f in (app\build\outputs\apk\release\*.apk) do (
    set APK_PATH=%%f
)

if "%APK_PATH%"=="" (
    echo [错误] 未找到 APK 文件
    pause
    exit /b 1
)

copy "%APK_PATH%" "AI-Assistant-Full.apk" >nul
echo.

REM 步骤 6: 创建安装包
echo [6/6] 创建完整安装包...
mkdir release 2>nul
copy "AI-Assistant-Full.apk" release\ >nul
copy README.md release\ >nul
copy install-guide.md release\ >nul
(
echo Android AI Assistant 安装说明
echo ==========================================
echo.
echo 文件清单：
echo - AI-Assistant-Full.apk (主应用)
echo - install-guide.md (安装指南)
echo.
echo 安装步骤：
echo 1. 在手机上启用"未知来源"安装
echo 2. 安装 AI-Assistant-Full.apk
echo 3. 首次运行授予 root 权限
echo 4. 下载 AI 模型
echo.
echo 模型下载：
echo - 在应用内点击"模型管理"
echo - 选择要下载的模型
echo - 等待下载完成
echo.
echo 使用说明：
echo - 输入文本与 AI 对话
echo - 使用 /help 查看命令
echo - 使用 /ls /cat 等命令管理文件
echo.
echo 注意事项：
echo - 需要 6GB+ 内存
echo - 需要 16GB+ 存储空间
echo - 首次下载模型需要网络
echo - 推荐使用 Qwen2.5-7B 模型
) > release\INSTALL.txt
echo.

echo ==========================================
echo   构建完成！
echo ==========================================
echo.
echo 输出目录: release\
echo - AI-Assistant-Full.apk
echo - install-guide.md
echo - INSTALL.txt
echo.
echo 下一步：
echo 1. 将 release\ 目录传输到手机
echo 2. 安装 APK
echo 3. 按照 INSTALL.txt 中的说明操作
echo.
pause
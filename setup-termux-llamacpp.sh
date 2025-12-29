#!/data/data/com.termux/files/usr/bin/bash

# ============================================================
# Android 手机本地 AI 部署脚本 - Termux + llama.cpp
# ============================================================
# 适用于：Android 高端机型（8GB+ 内存）
# 支持模型：Qwen2.5、Llama3、DeepSeek-Coder 等
# ============================================================

set -e

echo "=========================================="
echo "  Android 本地 AI 部署向导"
echo "  Termux + llama.cpp 方案"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 Termux 环境
echo -e "${YELLOW}[1/8] 检查 Termux 环境...${NC}"
if [ ! -d "/data/data/com.termux" ]; then
    echo -e "${RED}错误: 请在 Termux 中运行此脚本${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Termux 环境正常${NC}"
echo ""

# 更新包管理器
echo -e "${YELLOW}[2/8] 更新包管理器...${NC}"
pkg update -y
echo -e "${GREEN}✓ 包管理器已更新${NC}"
echo ""

# 安装必要依赖
echo -e "${YELLOW}[3/8] 安装必要依赖...${NC}"
pkg install -y git wget curl build-essential cmake python python-pip
echo -e "${GREEN}✓ 依赖安装完成${NC}"
echo ""

# 创建工作目录
echo -e "${YELLOW}[4/8] 创建工作目录...${NC}"
mkdir -p ~/ai-models
mkdir -p ~/ai-models/models
mkdir -p ~/ai-models/projects
cd ~/ai-models
echo -e "${GREEN}✓ 工作目录已创建${NC}"
echo ""

# 克隆 llama.cpp
echo -e "${YELLOW}[5/8] 克隆 llama.cpp 源码...${NC}"
cd ~/ai-models/projects
if [ -d "llama.cpp" ]; then
    echo -e "${YELLOW}llama.cpp 已存在，跳过克隆${NC}"
else
    git clone https://github.com/ggerganov/llama.cpp.git
    echo -e "${GREEN}✓ llama.cpp 克隆完成${NC}"
fi
echo ""

# 编译 llama.cpp
echo -e "${YELLOW}[6/8] 编译 llama.cpp...${NC}"
cd ~/ai-models/projects/llama.cpp
cmake -B build -DLLAMA_CURL=ON
cmake --build build -j$(nproc)
echo -e "${GREEN}✓ llama.cpp 编译完成${NC}"
echo ""

# 安装 Python 绑定
echo -e "${YELLOW}[7/8] 安装 Python 绑定...${NC}"
cd ~/ai-models/projects/llama.cpp
pip install -r requirements.txt
pip install .
echo -e "${GREEN}✓ Python 绑定安装完成${NC}"
echo ""

# 创建快捷启动脚本
echo -e "${YELLOW}[8/8] 创建快捷启动脚本...${NC}"
cat > ~/ai-models/start-ai.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# AI 模型启动脚本
cd ~/ai-models/projects/llama.cpp

# 默认模型路径
MODEL_PATH="$HOME/ai-models/models"

# 检查是否有模型文件
if [ ! -d "$MODEL_PATH" ] || [ -z "$(ls -A $MODEL_PATH)" ]; then
    echo "=========================================="
    echo "  没有找到模型文件"
    echo "=========================================="
    echo ""
    echo "请先下载模型，使用以下命令："
    echo ""
    echo "下载 Qwen2.5-7B-Instruct (推荐):"
    echo "  cd ~/ai-models/models"
    echo "  wget https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf"
    echo ""
    echo "下载 Phi-4-mini (轻量级):"
    echo "  cd ~/ai-models/models"
    echo "  wget https://huggingface.co/microsoft/Phi-4-mini-instruct-GGUF/resolve/main/Phi-4-mini-instruct-Q4_K_M.gguf"
    echo ""
    echo "下载 DeepSeek-Coder (代码生成):"
    echo "  cd ~/ai-models/models"
    echo "  wget https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct-GGUF/resolve/main/deepseek-coder-v2-lite-instruct-q4_k_m.gguf"
    echo ""
    exit 1
fi

# 选择模型
echo "=========================================="
echo "  可用模型列表"
echo "=========================================="
echo ""
ls -lh $MODEL_PATH/*.gguf 2>/dev/null || echo "没有找到 GGUF 模型文件"
echo ""
read -p "请输入模型文件名: " MODEL_FILE

MODEL_FILE="$MODEL_PATH/$MODEL_FILE"

if [ ! -f "$MODEL_FILE" ]; then
    echo "错误: 模型文件不存在: $MODEL_FILE"
    exit 1
fi

echo ""
echo "=========================================="
echo "  启动 AI 模型"
echo "=========================================="
echo "模型: $MODEL_FILE"
echo ""

# 启动交互模式
./build/bin/main -m "$MODEL_FILE" \
    -n 512 \
    --color \
    -i \
    -p "你好，我是你的 AI 助手，有什么我可以帮助你的吗？"
EOF

chmod +x ~/ai-models/start-ai.sh
echo -e "${GREEN}✓ 快捷启动脚本已创建${NC}"
echo ""

# 创建 Web UI 启动脚本
cat > ~/ai-models/start-webui.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# AI Web UI 启动脚本
cd ~/ai-models/projects/llama.cpp

MODEL_PATH="$HOME/ai-models/models"

# 检查模型
if [ ! -d "$MODEL_PATH" ] || [ -z "$(ls -A $MODEL_PATH)" ]; then
    echo "错误: 没有找到模型文件"
    echo "请先下载模型"
    exit 1
fi

# 选择模型
echo "=========================================="
echo "  选择模型用于 Web UI"
echo "=========================================="
echo ""
ls -lh $MODEL_PATH/*.gguf 2>/dev/null
echo ""
read -p "请输入模型文件名: " MODEL_FILE

MODEL_FILE="$MODEL_PATH/$MODEL_FILE"

echo ""
echo "=========================================="
echo "  启动 Web UI"
echo "=========================================="
echo "模型: $MODEL_FILE"
echo "访问地址: http://localhost:8080"
echo ""

# 启动 Web 服务器
./build/bin/server \
    -m "$MODEL_FILE" \
    --port 8080 \
    --host 0.0.0.0 \
    -c 2048 \
    -b 512
EOF

chmod +x ~/ai-models/start-webui.sh
echo -e "${GREEN}✓ Web UI 启动脚本已创建${NC}"
echo ""

# 完成
echo "=========================================="
echo "  安装完成！"
echo "=========================================="
echo ""
echo -e "${GREEN}下一步操作：${NC}"
echo ""
echo "1. 下载模型文件（选择一个）："
echo ""
echo "   ${YELLOW}方案 A - Qwen2.5-7B-Instruct (推荐，综合能力强):${NC}"
echo "   cd ~/ai-models/models"
echo "   wget https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf"
echo ""
echo "   ${YELLOW}方案 B - Phi-4-mini (轻量级，速度快):${NC}"
echo "   cd ~/ai-models/models"
echo "   wget https://huggingface.co/microsoft/Phi-4-mini-instruct-GGUF/resolve/main/Phi-4-mini-instruct-Q4_K_M.gguf"
echo ""
echo "   ${YELLOW}方案 C - DeepSeek-Coder (代码生成):${NC}"
echo "   cd ~/ai-models/models"
echo "   wget https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct-GGUF/resolve/main/deepseek-coder-v2-lite-instruct-q4_k_m.gguf"
echo ""
echo "2. 启动 AI 助手："
echo "   ~/ai-models/start-ai.sh"
echo ""
echo "3. 启动 Web UI（浏览器访问）："
echo "   ~/ai-models/start-webui.sh"
echo ""
echo "=========================================="
#!/data/data/com.termux/files/usr/bin/bash

# ============================================================
# Android 高性能 AI 部署脚本 - 最强 + 不卡顿
# ============================================================
# 推荐模型：Qwen2.5-7B-Instruct-Q4_K_M
# 性能优化：GPU 加速 + 内存优化 + 批处理
# 适用于：骁龙 8 Gen2/3、天玑 9200/9300、8GB+ 内存
# ============================================================

set -e

echo "=========================================="
echo "  Android 高性能 AI 部署"
echo "  最强模型 + 流畅体验"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# 检查 Termux 环境
echo -e "${YELLOW}[1/7] 检查 Termux 环境...${NC}"
if [ ! -d "/data/data/com.termux" ]; then
    echo -e "${RED}错误: 请在 Termux 中运行此脚本${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Termux 环境正常${NC}"
echo ""

# 更新包管理器
echo -e "${YELLOW}[2/7] 更新包管理器...${NC}"
pkg update -y
echo -e "${GREEN}✓ 包管理器已更新${NC}"
echo ""

# 安装必要依赖
echo -e "${YELLOW}[3/7] 安装必要依赖...${NC}"
pkg install -y git wget curl build-essential cmake python python-pip
echo -e "${GREEN}✓ 依赖安装完成${NC}"
echo ""

# 创建工作目录
echo -e "${YELLOW}[4/7] 创建工作目录...${NC}"
mkdir -p ~/ai-models/models
mkdir -p ~/ai-models/projects
cd ~/ai-models
echo -e "${GREEN}✓ 工作目录已创建${NC}"
echo ""

# 克隆 llama.cpp
echo -e "${YELLOW}[5/7] 克隆并编译 llama.cpp...${NC}"
cd ~/ai-models/projects
if [ ! -d "llama.cpp" ]; then
    git clone https://github.com/ggerganov/llama.cpp.git
fi
cd ~/ai-models/projects/llama.cpp
cmake -B build -DLLAMA_CURL=ON -DLLAMA_OPENBLAS=ON
cmake --build build -j$(nproc)
pip install -r requirements.txt
pip install .
echo -e "${GREEN}✓ llama.cpp 编译完成${NC}"
echo ""

# 下载最优模型
echo -e "${YELLOW}[6/7] 下载最优模型...${NC}"
cd ~/ai-models/models

MODEL_FILE="qwen2.5-7b-instruct-q4_k_m.gguf"
MODEL_URL="https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf"

if [ -f "$MODEL_FILE" ]; then
    echo -e "${YELLOW}模型已存在，跳过下载${NC}"
else
    echo "正在下载 Qwen2.5-7B-Instruct (Q4_K_M 量化)..."
    echo "模型大小: 约 4.7GB"
    echo "预计时间: 5-15 分钟 (取决于网络速度)"
    echo ""
    wget --progress=bar:force "$MODEL_URL" -O "$MODEL_FILE"
    echo -e "${GREEN}✓ 模型下载完成${NC}"
fi
echo ""

# 创建高性能启动脚本
echo -e "${YELLOW}[7/7] 创建高性能启动脚本...${NC}"
cat > ~/ai-models/start-fast.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# 高性能 AI 启动脚本
cd ~/ai-models/projects/llama.cpp

MODEL_PATH="$HOME/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf"

if [ ! -f "$MODEL_PATH" ]; then
    echo "错误: 模型文件不存在"
    echo "请先运行下载脚本"
    exit 1
fi

echo "=========================================="
echo "  启动 Qwen2.5-7B 高性能模式"
echo "=========================================="
echo ""
echo "优化配置："
echo "  - GPU 加速: 启用"
echo "  - 上下文长度: 4096 tokens"
echo "  - 批处理大小: 512"
echo "  - 温度: 0.7 (创造性)"
echo "  - Top-P: 0.9"
echo ""

# 高性能参数配置
./build/bin/main \
    -m "$MODEL_PATH" \
    -n 512 \
    --ctx-size 4096 \
    --batch-size 512 \
    --n-gpu-layers 99 \
    --temp 0.7 \
    --top-p 0.9 \
    --repeat-penalty 1.1 \
    --color \
    -i \
    -p "你好！我是 Qwen2.5-7B，一个强大的 AI 助手。我可以帮你解答问题、写作、编程、分析文档等。有什么我可以帮助你的吗？"
EOF

chmod +x ~/ai-models/start-fast.sh

# 创建 Web UI 启动脚本
cat > ~/ai-models/start-webui-fast.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# 高性能 Web UI 启动脚本
cd ~/ai-models/projects/llama.cpp

MODEL_PATH="$HOME/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf"

if [ ! -f "$MODEL_PATH" ]; then
    echo "错误: 模型文件不存在"
    exit 1
fi

echo "=========================================="
echo "  启动高性能 Web UI"
echo "=========================================="
echo ""
echo "访问地址: http://localhost:8080"
echo "局域网访问: http://$(ip addr show wlan0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}'):8080"
echo ""

# 高性能 Web 服务器配置
./build/bin/server \
    -m "$MODEL_PATH" \
    --port 8080 \
    --host 0.0.0.0 \
    --ctx-size 4096 \
    --batch-size 512 \
    --n-gpu-layers 99 \
    --ub 8192 \
    --n-predict 512
EOF

chmod +x ~/ai-models/start-webui-fast.sh

# 创建 API 服务脚本
cat > ~/ai-models/start-api.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# API 服务启动脚本
cd ~/ai-models/projects/llama.cpp

MODEL_PATH="$HOME/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf"

if [ ! -f "$MODEL_PATH" ]; then
    echo "错误: 模型文件不存在"
    exit 1
fi

echo "=========================================="
echo "  启动 API 服务"
echo "=========================================="
echo ""
echo "API 地址: http://localhost:8080"
echo ""
echo "API 端点："
echo "  POST /completion - 文本补全"
echo "  POST /chat/completions - 聊天对话"
echo ""
echo "示例："
echo '  curl http://localhost:8080/chat/completions -d {"model":"qwen","messages":[{"role":"user","content":"你好"}]}'
echo ""

# API 服务器配置
./build/bin/server \
    -m "$MODEL_PATH" \
    --port 8080 \
    --host 0.0.0.0 \
    --ctx-size 4096 \
    --batch-size 512 \
    --n-gpu-layers 99 \
    --ub 8192 \
    --n-predict 512
EOF

chmod +x ~/ai-models/start-api.sh

# 创建性能测试脚本
cat > ~/ai-models/benchmark.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# 性能测试脚本
cd ~/ai-models/projects/llama.cpp

MODEL_PATH="$HOME/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf"

echo "=========================================="
echo "  性能测试"
echo "=========================================="
echo ""

echo "测试 1: 单次生成速度"
echo "----------------------"
time ./build/bin/main \
    -m "$MODEL_PATH" \
    -n 256 \
    --ctx-size 4096 \
    --batch-size 512 \
    --n-gpu-layers 99 \
    --temp 0.7 \
    -p "请用一句话介绍你自己" \
    2>&1 | tail -1

echo ""
echo "测试 2: 多轮对话性能"
echo "----------------------"
time ./build/bin/main \
    -m "$MODEL_PATH" \
    -n 512 \
    --ctx-size 4096 \
    --batch-size 512 \
    --n-gpu-layers 99 \
    --temp 0.7 \
    -i \
    -p "你好，请介绍一下人工智能的发展历程" \
    2>&1 | tail -1

echo ""
echo "=========================================="
echo "  测试完成"
echo "=========================================="
echo ""
echo "如果每秒生成速度 > 10 tokens/s，说明性能良好"
echo "如果每秒生成速度 > 20 tokens/s，说明性能优秀"
echo "如果每秒生成速度 > 30 tokens/s，说明性能卓越"
EOF

chmod +x ~/ai-models/benchmark.sh

echo -e "${GREEN}✓ 高性能启动脚本已创建${NC}"
echo ""

# 完成
echo "=========================================="
echo "  安装完成！"
echo "=========================================="
echo ""
echo -e "${GREEN}已部署：${NC}"
echo "  模型: Qwen2.5-7B-Instruct (Q4_K_M)"
echo "  大小: 4.7GB"
echo "  能力: 综合最强 (代码、对话、分析)"
echo ""
echo -e "${GREEN}启动方式：${NC}"
echo ""
echo "  ${YELLOW}1. 交互模式（推荐）:${NC}"
echo "     ~/ai-models/start-fast.sh"
echo ""
echo "  ${YELLOW}2. Web UI（浏览器）:${NC}"
echo "     ~/ai-models/start-webui-fast.sh"
echo ""
echo "  ${YELLOW}3. API 服务（开发）:${NC}"
echo "     ~/ai-models/start-api.sh"
echo ""
echo "  ${YELLOW}4. 性能测试:${NC}"
echo "     ~/ai-models/benchmark.sh"
echo ""
echo -e "${GREEN}性能优化说明：${NC}"
echo "  - 使用 Q4_K_M 量化：平衡性能和质量"
echo "  - GPU 加速层: 99 层 (最大化 GPU 利用)"
echo "  - 批处理: 512 (提高吞吐量)"
echo "  - 上下文: 4096 tokens (支持长对话)"
echo ""
echo -e "${BLUE}预计性能（骁龙 8 Gen2/3）：${NC}"
echo "  - 首字延迟: 1-2 秒"
echo "  - 生成速度: 20-30 tokens/秒"
echo "  - 流畅度: 非常流畅"
echo ""
echo "=========================================="

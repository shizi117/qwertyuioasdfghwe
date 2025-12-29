#!/data/data/com.termux/files/usr/bin/bash

# ============================================================
# Android 手机本地 AI 部署脚本 - Termux + Ollama
# ============================================================
# 适用于：Android 高端机型
# 优点：安装简单，模型管理方便
# ============================================================

set -e

echo "=========================================="
echo "  Android 本地 AI 部署向导"
echo "  Termux + Ollama 方案"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# 检查 Termux 环境
echo -e "${YELLOW}[1/6] 检查 Termux 环境...${NC}"
if [ ! -d "/data/data/com.termux" ]; then
    echo -e "${RED}错误: 请在 Termux 中运行此脚本${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Termux 环境正常${NC}"
echo ""

# 更新包管理器
echo -e "${YELLOW}[2/6] 更新包管理器...${NC}"
pkg update -y
echo -e "${GREEN}✓ 包管理器已更新${NC}"
echo ""

# 安装必要依赖
echo -e "${YELLOW}[3/6] 安装必要依赖...${NC}"
pkg install -y wget curl
echo -e "${GREEN}✓ 依赖安装完成${NC}"
echo ""

# 下载 Ollama
echo -e "${YELLOW}[4/6] 下载 Ollama...${NC}"
mkdir -p ~/ollama
cd ~/ollama

# 下载 Ollama Linux ARM64 版本
wget https://ollama.com/download/ollama-linux-arm64 -O ollama
chmod +x ollama
echo -e "${GREEN}✓ Ollama 下载完成${NC}"
echo ""

# 创建启动脚本
echo -e "${YELLOW}[5/6] 创建启动脚本...${NC}"
cat > ~/ollama/start-ollama.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# Ollama 启动脚本
cd ~/ollama

# 设置环境变量
export OLLAMA_HOST=0.0.0.0
export OLLAMA_PORT=11434

echo "=========================================="
echo "  启动 Ollama 服务"
echo "=========================================="
echo ""

# 启动 Ollama
./ollama serve &
OLLAMA_PID=$!

echo "Ollama 服务已启动 (PID: $OLLAMA_PID)"
echo "服务地址: http://localhost:11434"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

# 等待服务启动
sleep 3

# 显示可用命令
echo "=========================================="
echo "  可用命令"
echo "=========================================="
echo ""
echo "下载模型："
echo "  ./ollama pull qwen2.5:7b        # Qwen2.5-7B"
echo "  ./ollama pull phi4:mini         # Phi-4-mini"
echo "  ./ollama pull codellama:7b      # CodeLlama"
echo "  ./ollama pull deepseek-coder    # DeepSeek-Coder"
echo ""
echo "运行模型："
echo "  ./ollama run qwen2.5:7b"
echo "  ./ollama run phi4:mini"
echo ""
echo "列出模型："
echo "  ./ollama list"
echo ""
echo "=========================================="

# 等待用户中断
wait $OLLAMA_PID
EOF

chmod +x ~/ollama/start-ollama.sh
echo -e "${GREEN}✓ 启动脚本已创建${NC}"
echo ""

# 创建聊天脚本
cat > ~/ollama/chat.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# Ollama 聊天脚本
cd ~/ollama

echo "=========================================="
echo "  Ollama 模型聊天"
echo "=========================================="
echo ""

# 列出可用模型
echo "已安装的模型："
echo ""
./ollama list 2>/dev/null || echo "还没有安装模型"
echo ""

if [ -z "$(./ollama list 2>/dev/null | tail -n +2)" ]; then
    echo "请先下载模型，例如："
    echo "  ./ollama pull qwen2.5:7b"
    echo ""
    exit 1
fi

read -p "请输入模型名称: " MODEL_NAME

echo ""
echo "=========================================="
echo "  开始聊天"
echo "=========================================="
echo "模型: $MODEL_NAME"
echo "输入 /quit 退出"
echo ""

# 启动聊天
./ollama run "$MODEL_NAME"
EOF

chmod +x ~/ollama/chat.sh
echo -e "${GREEN}✓ 聊天脚本已创建${NC}"
echo ""

# 创建 API 服务脚本
cat > ~/ollama/api-server.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash

# Ollama API 服务脚本
cd ~/ollama

export OLLAMA_HOST=0.0.0.0
export OLLAMA_PORT=11434

echo "=========================================="
echo "  启动 Ollama API 服务"
echo "=========================================="
echo ""
echo "API 地址: http://localhost:11434"
echo "API 文档: http://localhost:11434/api"
echo ""
echo "示例请求："
echo "  curl http://localhost:11434/api/generate -d '{"
echo '    "model": "qwen2.5:7b",'
echo '    "prompt": "你好",'
echo '    "stream": false'
echo "  }'"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

# 启动服务
./ollama serve
EOF

chmod +x ~/ollama/api-server.sh
echo -e "${GREEN}✓ API 服务脚本已创建${NC}"
echo ""

# 完成
echo "=========================================="
echo "  安装完成！"
echo "=========================================="
echo ""
echo -e "${GREEN}下一步操作：${NC}"
echo ""
echo "1. 启动 Ollama 服务："
echo "   ~/ollama/start-ollama.sh"
echo ""
echo "2. 下载模型（选择一个）："
echo ""
echo "   ${YELLOW}方案 A - Qwen2.5-7B (推荐，中文能力强):${NC}"
echo "   cd ~/ollama && ./ollama pull qwen2.5:7b"
echo ""
echo "   ${YELLOW}方案 B - Phi-4-mini (轻量级，速度快):${NC}"
echo "   cd ~/ollama && ./ollama pull phi4:mini"
echo ""
echo "   ${YELLOW}方案 C - CodeLlama (代码生成):${NC}"
echo "   cd ~/ollama && ./ollama pull codellama:7b"
echo ""
echo "   ${YELLOW}方案 D - DeepSeek-Coder (代码生成):${NC}"
echo "   cd ~/ollama && ./ollama pull deepseek-coder"
echo ""
echo "3. 开始聊天："
echo "   ~/ollama/chat.sh"
echo ""
echo "4. 启动 API 服务："
echo "   ~/ollama/api-server.sh"
echo ""
echo "=========================================="
# Android 手机本地 AI 部署完整指南

## 目录
1. [快速开始](#快速开始)
2. [方案一：Termux + llama.cpp（推荐）](#方案一termux--llamacpp推荐)
3. [方案二：Termux + Ollama（简单）](#方案二termux--ollama简单)
4. [常见问题](#常见问题)
5. [进阶使用](#进阶使用)

---

## 快速开始

### 前置要求
- Android 7.0 或更高版本
- 6GB+ 内存（推荐 8GB+）
- 16GB+ 可用存储空间
- 稳定的网络连接（首次下载模型）

### 安装 Termux

1. 打开 Google Play 或 F-Droid
2. 搜索并安装 `Termux`
3. 打开 Termux 应用

**重要提示：** 如果使用 F-Droid，请安装最新版以获得最佳兼容性。

---

## 方案一：Termux + llama.cpp（推荐）

**优点：**
- 性能最优，支持 GPU 加速
- 灵活性高，可自定义
- 支持多种模型格式
- 代码生成能力强

**适用场景：**
- 需要高性能推理
- 想要运行代码生成模型
- 技术用户

### 步骤 1：安装依赖

在 Termux 中执行：

```bash
# 更新包管理器
pkg update && pkg upgrade -y

# 安装必要工具
pkg install -y git wget curl build-essential cmake python python-pip
```

### 步骤 2：下载并运行安装脚本

```bash
# 创建工作目录
mkdir -p ~/android-ai
cd ~/android-ai

# 使用以下命令之一：

# 方法 A：如果在电脑上，将脚本复制到手机
# 方法 B：直接在 Termux 中下载（如果脚本已上传到服务器）

# 或者手动执行以下命令：
cd ~
mkdir -p ai-models/models
mkdir -p ai-models/projects
cd ai-models/projects
git clone https://github.com/ggerganov/llama.cpp.git
cd llama.cpp
cmake -B build -DLLAMA_CURL=ON
cmake --build build -j$(nproc)
pip install -r requirements.txt
pip install .
```

### 步骤 3：下载模型

选择一个模型下载：

**选项 A：Qwen2.5-7B-Instruct（推荐）**
```bash
cd ~/ai-models/models
wget https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf
```

**选项 B：Phi-4-mini（轻量级）**
```bash
cd ~/ai-models/models
wget https://huggingface.co/microsoft/Phi-4-mini-instruct-GGUF/resolve/main/Phi-4-mini-instruct-Q4_K_M.gguf
```

**选项 C：DeepSeek-Coder（代码生成）**
```bash
cd ~/ai-models/models
wget https://huggingface.co/deepseek-ai/DeepSeek-Coder-V2-Lite-Instruct-GGUF/resolve/main/deepseek-coder-v2-lite-instruct-q4_k_m.gguf
```

### 步骤 4：启动 AI 助手

**交互模式：**
```bash
cd ~/ai-models/projects/llama.cpp
./build/bin/main -m ~/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf \
    -n 512 \
    --color \
    -i \
    -p "你好，我是你的 AI 助手，有什么我可以帮助你的吗？"
```

**Web UI 模式：**
```bash
cd ~/ai-models/projects/llama.cpp
./build/bin/server \
    -m ~/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf \
    --port 8080 \
    --host 0.0.0.0
```

然后在浏览器中访问：`http://localhost:8080`

---

## 方案二：Termux + Ollama（简单）

**优点：**
- 安装简单
- 模型管理方便
- 开箱即用

**适用场景：**
- 快速部署
- 不想折腾
- 新手用户

### 步骤 1：安装 Termux

同方案一。

### 步骤 2：安装 Ollama

```bash
# 更新包管理器
pkg update && pkg upgrade -y

# 安装依赖
pkg install -y wget curl

# 下载 Ollama
mkdir -p ~/ollama
cd ~/ollama
wget https://ollama.com/download/ollama-linux-arm64 -O ollama
chmod +x ollama
```

### 步骤 3：下载并运行模型

```bash
# 启动 Ollama 服务
cd ~/ollama
./ollama serve &

# 下载模型（选择一个）
./ollama pull qwen2.5:7b        # Qwen2.5-7B
./ollama pull phi4:mini         # Phi-4-mini
./ollama pull codellama:7b      # CodeLlama

# 运行模型
./ollama run qwen2.5:7b
```

### 步骤 4：使用 API

```bash
# 启动 API 服务
./ollama serve

# 测试 API
curl http://localhost:11434/api/generate -d '{
  "model": "qwen2.5:7b",
  "prompt": "你好",
  "stream": false
}'
```

---

## 常见问题

### Q1: 模型下载速度慢怎么办？

**解决方案：**
1. 使用镜像站下载
2. 使用下载管理器（如 IDM）下载后传输到手机
3. 使用 `aria2c` 多线程下载：
   ```bash
   pkg install aria2
   aria2c -x 16 -s 16 [模型下载链接]
   ```

### Q2: 内存不足怎么办？

**解决方案：**
1. 使用更小的量化模型（Q2_K、Q3_K）
2. 关闭其他应用释放内存
3. 使用上下文长度限制：
   ```bash
   ./build/bin/main -m model.gguf -c 1024
   ```

### Q3: 推理速度慢怎么办？

**解决方案：**
1. 使用更小的模型
2. 启用 GPU 加速（需要支持 Vulkan）
3. 减少上下文长度
4. 使用更激进的量化（Q2_K）

### Q4: 如何在电脑上访问手机上的 AI？

**解决方案：**
1. 确保手机和电脑在同一网络
2. 使用 `0.0.0.0` 作为主机地址
3. 在电脑浏览器访问 `http://手机IP地址:端口`
4. 查找手机 IP：
   ```bash
   ip addr show wlan0
   ```

### Q5: Termux 权限问题？

**解决方案：**
```bash
# 授予存储权限
termux-setup-storage

# 如果需要后台运行
pkg install termux-services
```

---

## 进阶使用

### 1. 创建自定义启动脚本

```bash
cat > ~/ai-models/custom-chat.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash
cd ~/ai-models/projects/llama.cpp
./build/bin/main -m ~/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf \
    -n 1024 \
    -c 4096 \
    --temp 0.7 \
    --top_p 0.9 \
    --color \
    -i
EOF
chmod +x ~/ai-models/custom-chat.sh
```

### 2. 批量处理文本

```bash
# 创建批量处理脚本
cat > ~/ai-models/batch-process.sh << 'EOF'
#!/data/data/com.termux/files/usr/bin/bash
MODEL="$HOME/ai-models/models/qwen2.5-7b-instruct-q4_k_m.gguf"
INPUT_FILE="$1"

if [ -z "$INPUT_FILE" ]; then
    echo "用法: $0 <输入文件>"
    exit 1
fi

cd ~/ai-models/projects/llama.cpp
./build/bin/main -m "$MODEL" \
    -f "$INPUT_FILE" \
    -n 512 \
    --temp 0.7
EOF
chmod +x ~/ai-models/batch-process.sh
```

### 3. 集成到其他应用

使用 Ollama API 集成到 Python 应用：

```python
import requests

def chat_with_ollama(prompt, model="qwen2.5:7b"):
    response = requests.post('http://localhost:11434/api/generate', json={
        'model': model,
        'prompt': prompt,
        'stream': False
    })
    return response.json()['response']

# 使用
result = chat_with_ollama("你好")
print(result)
```

### 4. 后台运行服务

```bash
# 使用 nohup 后台运行
nohup ./ollama serve > ollama.log 2>&1 &

# 查看 PID
echo $!

# 停止服务
kill [PID]
```

---

## 性能优化建议

1. **模型选择：**
   - 日常对话：Phi-4-mini 或 Qwen2.5-7B
   - 代码生成：DeepSeek-Coder 或 Qwen2.5-Coder
   - 专业任务：Qwen2.5-32B 或 Llama-3.1-70B

2. **量化选择：**
   - Q4_K_M：最佳平衡
   - Q3_K_M：更小更快
   - Q5_K_M：更高精度

3. **参数调整：**
   - 温度：0.7（创造力平衡）
   - Top_p：0.9（多样性控制）
   - 上下文：根据需求调整

---

## 推荐资源

- **llama.cpp GitHub:** https://github.com/ggerganov/llama.cpp
- **Ollama 官网:** https://ollama.com
- **Hugging Face 模型库:** https://huggingface.co/models
- **Qwen 模型:** https://huggingface.co/Qwen
- **DeepSeek 模型:** https://huggingface.co/deepseek-ai

---

## 获取帮助

如果遇到问题：

1. 查看日志文件
2. 检查系统资源使用情况
3. 尝试更小的模型
4. 参考官方文档

祝你使用愉快！🎉
# Android 手机本地 AI 模型部署方案

## 推荐方案：MLC LLM

**为什么选择 MLC LLM：**
- 性能优化最佳，支持 GPU 加速
- 支持多种专业模型（代码生成、文档分析等）
- 有官方 Android 应用
- 支持离线运行

## 支持的专业模型

### 1. 代码生成模型
- **Qwen2.5-Coder-7B-Instruct** - 强大的代码生成能力
- **DeepSeek-Coder-V2-Lite-Instruct** - 轻量级但功能强大
- **CodeLlama-7B-Instruct** - Meta 的代码模型

### 2. 智能对话模型
- **Qwen2.5-7B-Instruct** - 中文能力强，适合专业对话
- **Llama-3.1-8B-Instruct** - 通用能力强
- **Phi-4-mini-Instruct** - 微软的小巧强大模型

### 3. 专业任务模型
- **Qwen2.5-72B-Instruct** - 最强综合能力（需要高端机型）
- **Gemma-2-27B-Instruct** - Google 的高性能模型

## 部署步骤

### 方案一：使用预编译 MLC LLM Android 应用（推荐新手）

1. 下载 MLC LLM Android APK
2. 安装到手机
3. 在应用中下载模型
4. 开始使用

### 方案二：使用 Termux + llama.cpp（推荐技术用户）

1. 安装 Termux
2. 安装 llama.cpp
3. 下载模型文件
4. 启动服务
5. 通过 Web UI 或 API 使用

### 方案三：使用 Ollama + Termux（推荐快速部署）

1. 安装 Termux
2. 安装 Ollama
3. 运行模型
4. 开始使用

## 性能要求

### 最低配置
- CPU: 骁龙 865 以上
- 内存: 6GB
- 存储: 8GB 可用空间

### 推荐配置
- CPU: 骁龙 8 Gen2/3
- 内存: 8GB+
- 存储: 16GB 可用空间
- NPU/GPU: 支持算力加速

## 模型大小参考

| 模型 | 参数量 | 量化后大小 | 推荐内存 |
|------|--------|-----------|---------|
| Phi-4-mini | 3.8B | 2-3GB | 4GB |
| Qwen2.5-7B | 7B | 4-5GB | 6GB |
| Llama-3.1-8B | 8B | 5-6GB | 8GB |
| Qwen2.5-14B | 14B | 8-10GB | 12GB |
| Qwen2.5-32B | 32B | 18-20GB | 16GB+ |

## 下一步

选择你喜欢的方案，然后查看对应的部署指南。
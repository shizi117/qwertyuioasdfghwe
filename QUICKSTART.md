# Android AI Assistant - 快速开始指南

## 项目概述

这是一个功能强大的 Android AI 助手应用，具有以下特性：

- ✅ **Root 权限支持** - 完全控制手机文件系统
- ✅ **本地 AI 推理** - 离线运行，无需网络
- ✅ **文件管理** - 查看、编辑、删除任意文件
- ✅ **命令执行** - 执行 root 命令
- ✅ **多模型支持** - 支持 Qwen、Llama、DeepSeek 等模型

## 系统要求

- Android 8.0+ (API 28+)
- Root 权限（必需）
- 6GB+ RAM（推荐 8GB+）
- 16GB+ 可用存储空间
- ARM64 架构

## 快速安装

### 方法一：使用预编译 APK（推荐）

1. **下载 APK**
   ```bash
   # 从 release 目录获取
   AI-Assistant-Full.apk
   ```

2. **安装到手机**
   - 启用"未知来源"安装
   - 安装 APK
   - 授予 root 权限

3. **下载模型**
   - 打开应用
   - 点击"模型管理"
   - 选择并下载模型

### 方法二：从源码构建

#### 前置要求

- Android SDK
- JDK 8+
- Gradle
- NDK（可选，用于编译 llama.cpp）

#### 构建步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd android-ai-deploy
   ```

2. **构建 APK**
   ```bash
   # Windows
   build-apk-with-llama.bat

   # Linux/Mac
   ./build-apk-with-llama.sh
   ```

3. **安装 APK**
   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

## 首次使用

### 1. 授予权限

首次启动时，应用会请求以下权限：

- **Root 权限** - 必需，用于文件操作和命令执行
- **存储权限** - 必需，用于访问文件系统
- **网络权限** - 可选，用于下载模型

### 2. 下载模型

推荐模型：

| 模型 | 大小 | 用途 | 速度 |
|------|------|------|------|
| Qwen2.5-7B | ~4.5GB | 通用对话 | 中等 |
| Phi-4-mini | ~2.3GB | 轻量对话 | 快 |
| DeepSeek-Coder | ~4.0GB | 代码生成 | 中等 |

下载方式：

```bash
# 在应用内
点击"模型管理" → 选择模型 → 等待下载

# 或使用命令
/model /sdcard/Download/qwen2.5-7b-instruct-q4_k_m.gguf
```

### 3. 开始使用

**对话模式：**
```
你好，请帮我写一段 Python 代码
```

**命令模式：**
```
/help          # 查看帮助
/ls /sdcard    # 列出文件
/cat file.txt  # 查看文件
/rm file.txt   # 删除文件
```

## 常用命令

### 文件操作

```bash
/ls [路径]              # 列出目录
/cat [文件]             # 查看文件
/rm [文件]              # 删除文件
/cp [源] [目标]         # 复制文件
/mv [源] [目标]         # 移动文件
/mkdir [路径]           # 创建目录
```

### 系统操作

```bash
/root [命令]            # 执行 root 命令
/ps                     # 查看进程
/kill [PID]             # 杀死进程
```

### 模型操作

```bash
/model [路径]           # 加载模型
/models                 # 列出可用模型
```

## 高级功能

### 1. 批量文件处理

```bash
# 删除所有 .tmp 文件
/root find /sdcard -name "*.tmp" -delete

# 批量重命名
/root for f in *.jpg; do mv "$f" "prefix_$f"; done
```

### 2. 系统监控

```bash
# 查看内存使用
/root free -h

# 查看磁盘使用
/root df -h

# 查看进程
/root ps aux
```

### 3. 安装/卸载应用

```bash
# 安装 APK
/root pm install /sdcard/Download/app.apk

# 卸载应用
/root pm uninstall com.example.app
```

### 4. 文件权限管理

```bash
# 修改权限
/root chmod 755 /sdcard/script.sh

# 修改所有者
/root chown root:root /sdcard/config.txt
```

## 故障排除

### 问题：无法获取 root 权限

**解决方案：**
1. 确认设备已 root
2. 检查 Magisk 版本
3. 在 Magisk 中授予应用 root 权限

### 问题：模型下载失败

**解决方案：**
1. 检查网络连接
2. 使用下载工具下载后手动复制到模型目录
3. 模型目录：`/sdcard/Android/data/com.android.aiassistant/files/models/`

### 问题：推理速度慢

**解决方案：**
1. 使用更小的模型（Phi-4-mini）
2. 减少上下文长度
3. 关闭其他应用释放内存

### 问题：应用闪退

**解决方案：**
1. 检查日志：`adb logcat | grep AIAssistant`
2. 确保有足够的存储空间
3. 清除应用数据后重试

## 安全提示

⚠️ **重要警告：**

1. **仅用于个人设备** - 不要在非个人设备上使用
2. **谨慎使用 root** - 错误的命令可能导致系统损坏
3. **备份重要数据** - 在执行危险操作前先备份
4. **不要分享 APK** - 包含你的配置和数据

## 开发者信息

- **包名：** com.android.aiassistant
- **版本：** 1.0.0
- **最低 SDK：** 28 (Android 8.0)
- **目标 SDK：** 34 (Android 14)

## 更新日志

### v1.0.0 (2024-12-29)
- ✨ 初始版本发布
- ✅ Root 权限支持
- ✅ 本地 AI 推理
- ✅ 文件管理功能
- ✅ 命令执行功能

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue。

---

**享受你的 AI 助手！** 🚀
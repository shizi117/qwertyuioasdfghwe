# GitHub Actions 自动构建 APK 指南

## 快速开始

### 第一步：创建 GitHub 仓库

1. 访问 https://github.com/new
2. 创建新仓库，命名为 `android-ai-assistant`
3. 选择 Public 或 Private（推荐 Private）
4. 不要初始化 README、.gitignore 或 license

### 第二步：上传代码

在项目目录执行以下命令：

```bash
cd C:\Users\Administrator\android-ai-deploy

# 初始化 Git
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: Android AI Assistant"

# 添加远程仓库（替换 YOUR_USERNAME）
git remote add origin https://github.com/YOUR_USERNAME/android-ai-assistant.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

### 第三步：触发构建

推送完成后，GitHub Actions 会自动开始构建：

1. 访问你的仓库：https://github.com/YOUR_USERNAME/android-ai-assistant
2. 点击 "Actions" 标签
3. 等待构建完成（约 5-10 分钟）

### 第四步：下载 APK

构建完成后：

1. 在 Actions 页面找到最新的构建
2. 点击进入构建详情
3. 在 "Artifacts" 部分下载 "AI-Assistant-APK"
4. 解压下载的文件，里面就是 APK

## 手动触发构建

如果你想手动触发构建：

1. 访问仓库的 Actions 页面
2. 选择 "Build Android APK" workflow
3. 点击 "Run workflow"
4. 选择分支，点击 "Run workflow"

## 发布版本

当你推送到 main 分支时，会自动创建 Release：

1. 访问仓库的 "Releases" 页面
2. 下载最新的 Release 中的 APK

## 常见问题

### Q: 构建失败怎么办？

A: 检查 Actions 日志：
1. 点击失败的构建
2. 查看错误信息
3. 常见问题：缺少文件、配置错误

### Q: 如何修改构建配置？

A: 编辑 `.github/workflows/build-apk.yml` 文件

### Q: 如何构建 Debug 版本？

A: 修改 workflow 文件中的 `assembleRelease` 为 `assembleDebug`

### Q: 如何添加签名？

A: 在 workflow 中添加签名配置（需要设置 GitHub Secrets）

## 推送到 GitHub

请按照以下步骤操作：

1. **创建 GitHub 账户**（如果没有）
   - 访问 https://github.com/signup

2. **创建仓库**
   - 访问 https://github.com/new
   - 仓库名：`android-ai-assistant`
   - 选择 Public 或 Private
   - 点击 "Create repository"

3. **配置 Git**（首次使用）
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```

4. **上传代码**
   ```bash
   cd C:\Users\Administrator\android-ai-deploy
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/android-ai-assistant.git
   git push -u origin main
   ```

   **注意：** 将 `YOUR_USERNAME` 替换为你的 GitHub 用户名

5. **等待构建**
   - 推送后约 5-10 分钟
   - 访问 Actions 页面查看进度

6. **下载 APK**
   - 构建完成后下载 Artifacts

## 需要帮助？

如果遇到问题，请告诉我具体的错误信息。